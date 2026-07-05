import { AdminGuard } from "@/components/auth/AuthGuard";
import { UserManagementPage } from "@/components/users/UserManagementPage";

export default function UsersPage() {
  return (
    <AdminGuard>
      <UserManagementPage />
    </AdminGuard>
  );
}
