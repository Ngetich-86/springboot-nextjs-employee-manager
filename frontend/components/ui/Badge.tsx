import { Role } from "@/types";
import styles from "./Badge.module.css";

type BadgeVariant = "admin" | "user" | "success" | "warning";

interface BadgeProps {
  variant: BadgeVariant;
  children: React.ReactNode;
}

export function Badge({ variant, children }: BadgeProps) {
  return <span className={`${styles.badge} ${styles[variant]}`}>{children}</span>;
}

export function RoleBadge({ role }: { role: Role }) {
  return (
    <Badge variant={role === Role.ADMIN ? "admin" : "user"}>
      {role === Role.ADMIN ? "Admin" : "User"}
    </Badge>
  );
}
