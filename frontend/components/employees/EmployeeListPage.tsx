"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import {
  FiArrowDown,
  FiArrowUp,
  FiDownload,
  FiEdit2,
  FiEye,
  FiPlus,
  FiTrash2,
} from "react-icons/fi";
import {
  deleteEmployee,
  exportEmployees,
  fetchEmployees,
} from "@/services/employeeService";
import { getErrorMessage } from "@/services/api";
import { useAuthStore } from "@/store/authStore";
import { useDebounce } from "@/hooks/useDebounce";
import { downloadBlob } from "@/lib/download";
import { formatCurrency, formatDate } from "@/utils/formatters";
import { Employee } from "@/types";
import { SearchInput } from "@/components/ui/SearchInput";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import { Pagination } from "@/components/ui/Pagination";
import { ConfirmModal } from "@/components/ui/Modal";
import { EmptyState, ErrorState } from "@/components/ui/State";
import styles from "./EmployeePages.module.css";
import tableStyles from "@/components/ui/Table.module.css";

type SortField =
  | "firstName"
  | "lastName"
  | "email"
  | "department"
  | "position"
  | "salary"
  | "hireDate"
  | "createdAt";

interface SortState {
  sortBy: SortField;
  sortDirection: "asc" | "desc";
}

export function EmployeeListPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const isAdmin = useAuthStore((state) => state.isAdmin());

  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState<SortState>({ sortBy: "createdAt", sortDirection: "desc" });
  const [deleteTarget, setDeleteTarget] = useState<Employee | null>(null);

  const debouncedSearch = useDebounce(search);

  const queryParams = useMemo(
    () => ({
      page,
      size,
      search: debouncedSearch || undefined,
      sortBy: sort.sortBy,
      sortDirection: sort.sortDirection,
    }),
    [page, size, debouncedSearch, sort],
  );

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["employees", queryParams],
    queryFn: () => fetchEmployees(queryParams),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteEmployee(id),
    onSuccess: () => {
      toast.success("Employee deleted successfully");
      setDeleteTarget(null);
      queryClient.invalidateQueries({ queryKey: ["employees"] });
    },
    onError: (deleteError) => {
      toast.error(getErrorMessage(deleteError, "Failed to delete employee"));
    },
  });

  const handleSort = (field: SortField) => {
    setSort((current) => ({
      sortBy: field,
      sortDirection:
        current.sortBy === field && current.sortDirection === "asc" ? "desc" : "asc",
    }));
    setPage(0);
  };

  const handleExport = async (format: "csv" | "json") => {
    try {
      const blob = await exportEmployees(format, debouncedSearch || undefined);
      downloadBlob(blob, `employees.${format}`);
      toast.success(`Employees exported as ${format.toUpperCase()}`);
    } catch (exportError) {
      toast.error(getErrorMessage(exportError, "Export failed"));
    }
  };

  const renderSortIcon = (field: SortField) => {
    if (sort.sortBy !== field) return null;
    return sort.sortDirection === "asc" ? (
      <FiArrowUp className={tableStyles.sortIcon} size={14} />
    ) : (
      <FiArrowDown className={tableStyles.sortIcon} size={14} />
    );
  };

  return (
    <div>
      <div className={styles.pageHeader}>
        <div>
          <h1 className="page-title">Employees</h1>
          <p className="page-subtitle">Manage and browse employee records</p>
        </div>
        {isAdmin ? (
          <Button onClick={() => router.push("/dashboard/employees/new")}>
            <FiPlus size={16} />
            Add Employee
          </Button>
        ) : null}
      </div>

      <div className="card">
        <div className="card-header">
          <SearchInput
            value={search}
            onChange={(value) => {
              setSearch(value);
              setPage(0);
            }}
            placeholder="Search employees..."
          />
          <div className={styles.toolbarActions}>
            <Button variant="secondary" size="sm" onClick={() => handleExport("csv")}>
              <FiDownload size={14} />
              CSV
            </Button>
            <Button variant="secondary" size="sm" onClick={() => handleExport("json")}>
              <FiDownload size={14} />
              JSON
            </Button>
          </div>
        </div>

        {isLoading ? (
          <div className="card-body text-center">
            <Spinner size="lg" />
          </div>
        ) : isError ? (
          <ErrorState message={getErrorMessage(error)} onRetry={() => refetch()} />
        ) : !data?.content.length ? (
          <EmptyState
            title="No employees found"
            description={
              debouncedSearch
                ? "Try adjusting your search terms."
                : "Get started by adding your first employee."
            }
            action={
              isAdmin ? (
                <Button onClick={() => router.push("/dashboard/employees/new")}>
                  <FiPlus size={16} />
                  Add Employee
                </Button>
              ) : undefined
            }
          />
        ) : (
          <>
            <div className={tableStyles.tableWrapper}>
              <table className={tableStyles.table}>
                <thead>
                  <tr>
                    {(
                      [
                        ["firstName", "Name"],
                        ["email", "Email"],
                        ["department", "Department"],
                        ["position", "Position"],
                        ["salary", "Salary"],
                        ["hireDate", "Hire Date"],
                      ] as const
                    ).map(([field, label]) => (
                      <th
                        key={field}
                        className={tableStyles.sortable}
                        onClick={() => handleSort(field)}
                      >
                        {label}
                        {renderSortIcon(field)}
                      </th>
                    ))}
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((employee) => (
                    <tr key={employee.id}>
                      <td>
                        {employee.firstName} {employee.lastName}
                      </td>
                      <td>{employee.email}</td>
                      <td>{employee.department}</td>
                      <td>{employee.position}</td>
                      <td>{formatCurrency(employee.salary)}</td>
                      <td>{formatDate(employee.hireDate)}</td>
                      <td>
                        <div className={tableStyles.actions}>
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label="View employee"
                            onClick={() => router.push(`/dashboard/employees/${employee.id}`)}
                          >
                            <FiEye size={16} />
                          </Button>
                          {isAdmin ? (
                            <>
                              <Button
                                variant="ghost"
                                size="icon"
                                aria-label="Edit employee"
                                onClick={() =>
                                  router.push(`/dashboard/employees/${employee.id}/edit`)
                                }
                              >
                                <FiEdit2 size={16} />
                              </Button>
                              <Button
                                variant="ghost"
                                size="icon"
                                aria-label="Delete employee"
                                onClick={() => setDeleteTarget(employee)}
                              >
                                <FiTrash2 size={16} />
                              </Button>
                            </>
                          ) : null}
                        </div>
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

      <ConfirmModal
        isOpen={Boolean(deleteTarget)}
        title="Delete Employee"
        message={
          deleteTarget
            ? `Are you sure you want to delete ${deleteTarget.firstName} ${deleteTarget.lastName}? This action cannot be undone.`
            : ""
        }
        confirmLabel="Delete"
        isLoading={deleteMutation.isPending}
        onConfirm={() => {
          if (deleteTarget) deleteMutation.mutate(deleteTarget.id);
        }}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
