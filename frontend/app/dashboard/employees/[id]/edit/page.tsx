import { AdminGuard } from "@/components/auth/AuthGuard";
import { EmployeeFormPage } from "@/components/employees/EmployeeFormPage";

interface EditEmployeeRouteProps {
  params: Promise<{ id: string }>;
}

export default async function EditEmployeeRoute({ params }: EditEmployeeRouteProps) {
  const { id } = await params;
  return (
    <AdminGuard>
      <EmployeeFormPage mode="edit" employeeId={Number(id)} />
    </AdminGuard>
  );
}
