"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import { createEmployee, fetchEmployee, updateEmployee } from "@/services/employeeService";
import { getErrorMessage } from "@/services/api";
import { EmployeeFormValues } from "@/types";
import { EmployeeForm } from "./EmployeeForm";
import { Spinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/State";
import styles from "./EmployeePages.module.css";

interface EmployeeFormPageProps {
  mode: "create" | "edit";
  employeeId?: number;
}

export function EmployeeFormPage({ mode, employeeId }: EmployeeFormPageProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const isEdit = mode === "edit";

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["employee", employeeId],
    queryFn: () => fetchEmployee(employeeId as number),
    enabled: isEdit && employeeId !== undefined,
  });

  const mutation = useMutation({
    mutationFn: (values: EmployeeFormValues) =>
      isEdit ? updateEmployee(employeeId as number, values) : createEmployee(values),
    onSuccess: (employee) => {
      toast.success(isEdit ? "Employee updated successfully" : "Employee created successfully");
      queryClient.invalidateQueries({ queryKey: ["employees"] });
      queryClient.invalidateQueries({ queryKey: ["employee", employee.id] });
      router.push(`/dashboard/employees/${employee.id}`);
    },
    onError: (submitError) => {
      toast.error(
        getErrorMessage(submitError, isEdit ? "Failed to update employee" : "Failed to create employee"),
      );
    },
  });

  return (
    <div>
      <div className={styles.pageHeader}>
        <div>
          <h1 className="page-title">{isEdit ? "Edit Employee" : "Add Employee"}</h1>
          <p className="page-subtitle">
            {isEdit ? "Update employee information" : "Create a new employee record"}
          </p>
        </div>
      </div>

      <div className="card card-body">
        {isEdit && isLoading ? (
          <div className="text-center">
            <Spinner size="lg" />
          </div>
        ) : isEdit && isError ? (
          <ErrorState message={getErrorMessage(error)} onRetry={() => refetch()} />
        ) : (
          <EmployeeForm
            defaultValues={
              data
                ? {
                    firstName: data.firstName,
                    lastName: data.lastName,
                    email: data.email,
                    department: data.department,
                    position: data.position,
                    salary: data.salary,
                    hireDate: data.hireDate,
                  }
                : undefined
            }
            onSubmit={(values) => mutation.mutate(values)}
            onCancel={() => router.back()}
            isSubmitting={mutation.isPending}
            submitLabel={isEdit ? "Update Employee" : "Create Employee"}
          />
        )}
      </div>
    </div>
  );
}
