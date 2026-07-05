"use client";

import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { FiArrowLeft, FiEdit2 } from "react-icons/fi";
import { fetchEmployee } from "@/services/employeeService";
import { getErrorMessage } from "@/services/api";
import { useAuthStore } from "@/store/authStore";
import { formatCurrency, formatDate, formatDateTime } from "@/utils/formatters";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/State";
import styles from "./EmployeePages.module.css";

interface EmployeeDetailPageProps {
  employeeId: number;
}

export function EmployeeDetailPage({ employeeId }: EmployeeDetailPageProps) {
  const router = useRouter();
  const isAdmin = useAuthStore((state) => state.isAdmin());

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["employee", employeeId],
    queryFn: () => fetchEmployee(employeeId),
  });

  return (
    <div>
      <div className={styles.pageHeader}>
        <div>
          <Button variant="ghost" size="sm" onClick={() => router.push("/dashboard/employees")}>
            <FiArrowLeft size={16} />
            Back to Employees
          </Button>
          <h1 className="page-title mt-2">Employee Details</h1>
        </div>
        {isAdmin && data ? (
          <Button onClick={() => router.push(`/dashboard/employees/${employeeId}/edit`)}>
            <FiEdit2 size={16} />
            Edit Employee
          </Button>
        ) : null}
      </div>

      <div className="card card-body">
        {isLoading ? (
          <div className="text-center">
            <Spinner size="lg" />
          </div>
        ) : isError ? (
          <ErrorState message={getErrorMessage(error)} onRetry={() => refetch()} />
        ) : data ? (
          <div className={styles.detailGrid}>
            <div className={styles.detailItem}>
              <label>Full Name</label>
              <p>
                {data.firstName} {data.lastName}
              </p>
            </div>
            <div className={styles.detailItem}>
              <label>Email</label>
              <p>{data.email}</p>
            </div>
            <div className={styles.detailItem}>
              <label>Department</label>
              <p>{data.department}</p>
            </div>
            <div className={styles.detailItem}>
              <label>Position</label>
              <p>{data.position}</p>
            </div>
            <div className={styles.detailItem}>
              <label>Salary</label>
              <p>{formatCurrency(data.salary)}</p>
            </div>
            <div className={styles.detailItem}>
              <label>Hire Date</label>
              <p>{formatDate(data.hireDate)}</p>
            </div>
            <div className={styles.detailItem}>
              <label>Created</label>
              <p>{formatDateTime(data.createdAt)}</p>
            </div>
            <div className={styles.detailItem}>
              <label>Last Updated</label>
              <p>{formatDateTime(data.updatedAt)}</p>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
