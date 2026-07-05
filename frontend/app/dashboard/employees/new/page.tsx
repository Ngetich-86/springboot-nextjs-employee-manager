import { AdminGuard } from "@/components/auth/AuthGuard";
import { EmployeeFormPage } from "@/components/employees/EmployeeFormPage";

export default function NewEmployeePage() {
  return (
    <AdminGuard>
      <EmployeeFormPage mode="create" />
    </AdminGuard>
  );
}
