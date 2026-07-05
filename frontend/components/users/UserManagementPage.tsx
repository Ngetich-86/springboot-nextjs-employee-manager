"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import { fetchUsers, updateUserRole } from "@/services/userService";
import { getErrorMessage } from "@/services/api";
import { useDebounce } from "@/hooks/useDebounce";
import { Role, User } from "@/types";
import { SearchInput } from "@/components/ui/SearchInput";
import { RoleBadge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { Pagination } from "@/components/ui/Pagination";
import { EmptyState, ErrorState } from "@/components/ui/State";
import styles from "@/components/employees/EmployeePages.module.css";
import tableStyles from "@/components/ui/Table.module.css";
import selectStyles from "./UserManagement.module.css";

export function UserManagementPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [updatingUserId, setUpdatingUserId] = useState<number | null>(null);

  const debouncedSearch = useDebounce(search);

  const queryParams = useMemo(
    () => ({
      page,
      size,
      search: debouncedSearch || undefined,
      sortBy: "createdAt",
      sortDirection: "desc" as const,
    }),
    [page, size, debouncedSearch],
  );

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["users", queryParams],
    queryFn: () => fetchUsers(queryParams),
  });

  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: number; role: Role }) => updateUserRole(id, role),
    onMutate: ({ id }) => setUpdatingUserId(id),
    onSuccess: (updatedUser) => {
      toast.success(`Role updated for ${updatedUser.email}`);
      queryClient.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (roleError) => {
      toast.error(getErrorMessage(roleError, "Failed to update role"));
    },
    onSettled: () => setUpdatingUserId(null),
  });

  const handleRoleChange = (user: User, nextRole: Role) => {
    if (user.role === nextRole) return;
    roleMutation.mutate({ id: user.id, role: nextRole });
  };

  return (
    <div>
      <div className={styles.pageHeader}>
        <div>
          <h1 className="page-title">User Management</h1>
          <p className="page-subtitle">Manage user accounts and roles</p>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <SearchInput
            value={search}
            onChange={(value) => {
              setSearch(value);
              setPage(0);
            }}
            placeholder="Search users by email..."
          />
        </div>

        {isLoading ? (
          <div className="card-body text-center">
            <Spinner size="lg" />
          </div>
        ) : isError ? (
          <ErrorState message={getErrorMessage(error)} onRetry={() => refetch()} />
        ) : !data?.content.length ? (
          <EmptyState
            title="No users found"
            description={
              debouncedSearch ? "Try adjusting your search terms." : "No users are registered yet."
            }
          />
        ) : (
          <>
            <div className={tableStyles.tableWrapper}>
              <table className={tableStyles.table}>
                <thead>
                  <tr>
                    <th>Email</th>
                    <th>Current Role</th>
                    <th>Change Role</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((user) => (
                    <tr key={user.id}>
                      <td>{user.email}</td>
                      <td>
                        <RoleBadge role={user.role} />
                      </td>
                      <td>
                        <select
                          className={selectStyles.select}
                          value={user.role}
                          disabled={updatingUserId === user.id}
                          onChange={(event) =>
                            handleRoleChange(user, event.target.value as Role)
                          }
                          aria-label={`Change role for ${user.email}`}
                        >
                          <option value={Role.USER}>User</option>
                          <option value={Role.ADMIN}>Admin</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="card-body" style={{ paddingTop: 0 }}>
              <Pagination
                page={data.page}
                totalPages={data.totalPages}
                totalElements={data.totalElements}
                size={data.size}
                onPageChange={setPage}
                onSizeChange={(nextSize) => {
                  setSize(nextSize);
                  setPage(0);
                }}
              />
            </div>
          </>
        )}
      </div>
    </div>
  );
}
