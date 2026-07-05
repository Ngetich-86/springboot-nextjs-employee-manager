"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { FiArrowRight, FiUsers, FiUserCheck } from "react-icons/fi";
import { fetchEmployees } from "@/services/employeeService";
import { useAuthStore } from "@/store/authStore";
import { RoleBadge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/State";
import { getErrorMessage } from "@/services/api";
import styles from "./DashboardHome.module.css";

export function DashboardHome() {
  const user = useAuthStore((state) => state.user);
  const isAdmin = useAuthStore((state) => state.isAdmin());

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["employees", "stats"],
    queryFn: () => fetchEmployees({ page: 0, size: 1 }),
  });

  return (
    <div>
      <div className="mb-3">
        <h1 className="page-title">Dashboard</h1>
        <p className="page-subtitle">
          Welcome back{user ? `, ${user.email.split("@")[0]}` : ""}. Here is your overview.
        </p>
      </div>

      <div className="grid-3 mb-3">
        <div className={`card ${styles.statCard}`}>
          <div className={styles.statLabel}>Your Role</div>
          <div className="mt-1">
            {user ? <RoleBadge role={user.role} /> : null}
          </div>
        </div>
        <div className={`card ${styles.statCard}`}>
          <div className={styles.statLabel}>Total Employees</div>
          {isLoading ? (
            <div className="mt-2">
              <Spinner />
            </div>
          ) : isError ? (
            <div className="mt-2 text-sm text-muted">Unable to load</div>
          ) : (
            <div className={styles.statValue}>{data?.totalElements ?? 0}</div>
          )}
        </div>
        <div className={`card ${styles.statCard}`}>
          <div className={styles.statLabel}>Access Level</div>
          <div className={`${styles.statValue} text-sm`} style={{ fontSize: "1.125rem" }}>
            {isAdmin ? "Full admin access" : "Read-only employee access"}
          </div>
        </div>
      </div>

      {isError ? (
        <div className="card mb-3">
          <ErrorState message={getErrorMessage(error)} onRetry={() => refetch()} />
        </div>
      ) : null}

      <div className="card card-body">
        <h2 className="card-title mb-2">Quick Actions</h2>
        <div className={styles.quickLinks}>
          <Link href="/dashboard/employees" className={styles.quickLink}>
            <div className={styles.quickIcon}>
              <FiUsers size={18} />
            </div>
            <div>
              <div className={styles.quickTitle}>Browse Employees</div>
              <div className={styles.quickDesc}>View, search, and export employee records</div>
            </div>
            <FiArrowRight />
          </Link>
          {isAdmin ? (
            <>
              <Link href="/dashboard/employees/new" className={styles.quickLink}>
                <div className={styles.quickIcon}>
                  <FiUsers size={18} />
                </div>
                <div>
                  <div className={styles.quickTitle}>Add Employee</div>
                  <div className={styles.quickDesc}>Create a new employee record</div>
                </div>
                <FiArrowRight />
              </Link>
              <Link href="/dashboard/users" className={styles.quickLink}>
                <div className={styles.quickIcon}>
                  <FiUserCheck size={18} />
                </div>
                <div>
                  <div className={styles.quickTitle}>Manage Users</div>
                  <div className={styles.quickDesc}>Update user roles and permissions</div>
                </div>
                <FiArrowRight />
              </Link>
            </>
          ) : null}
        </div>
      </div>
    </div>
  );
}
