import { api } from "./api";
import {
  ApiResponse,
  Employee,
  EmployeeFormValues,
  PageResponse,
  PaginationParams,
} from "@/types";

function buildParams(params: PaginationParams): Record<string, string | number> {
  const query: Record<string, string | number> = {};
  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.sortBy) query.sortBy = params.sortBy;
  if (params.sortDirection) query.sortDirection = params.sortDirection;
  if (params.search) query.search = params.search;
  return query;
}

export async function fetchEmployees(
  params: PaginationParams,
): Promise<PageResponse<Employee>> {
  const response = await api.get<ApiResponse<PageResponse<Employee>>>("/api/v1/employees", {
    params: buildParams(params),
  });
  return response.data.data;
}

export async function fetchEmployee(id: number): Promise<Employee> {
  const response = await api.get<ApiResponse<Employee>>(`/api/v1/employees/${id}`);
  return response.data.data;
}

export async function createEmployee(payload: EmployeeFormValues): Promise<Employee> {
  const response = await api.post<ApiResponse<Employee>>("/api/v1/employees", payload);
  return response.data.data;
}

export async function updateEmployee(
  id: number,
  payload: EmployeeFormValues,
): Promise<Employee> {
  const response = await api.put<ApiResponse<Employee>>(`/api/v1/employees/${id}`, payload);
  return response.data.data;
}

export async function deleteEmployee(id: number): Promise<void> {
  await api.delete(`/api/v1/employees/${id}`);
}

export async function exportEmployees(
  format: "csv" | "json",
  search?: string,
): Promise<Blob> {
  const response = await api.get(`/api/v1/employees/export/${format}`, {
    params: search ? { search } : undefined,
    responseType: "blob",
  });
  return response.data;
}
