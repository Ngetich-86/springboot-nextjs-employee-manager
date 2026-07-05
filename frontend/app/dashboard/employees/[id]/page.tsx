import { EmployeeDetailPage } from "@/components/employees/EmployeeDetailPage";

interface EmployeeDetailRouteProps {
  params: Promise<{ id: string }>;
}

export default async function EmployeeDetailRoute({ params }: EmployeeDetailRouteProps) {
  const { id } = await params;
  return <EmployeeDetailPage employeeId={Number(id)} />;
}
