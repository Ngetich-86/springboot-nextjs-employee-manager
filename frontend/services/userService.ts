import { api } from "./api";
import { ApiResponse, PageResponse, PaginationParams, Role, User } from "@/types";

function buildParams(params: PaginationParams): Record<string, string | number> {
  const query: Record<string, string | number> = {};
  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.sortBy) query.sortBy = params.sortBy;
  if (params.sortDirection) query.sortDirection = params.sortDirection;
  if (params.search) query.search = params.search;
  return query;
}

export async function fetchUsers(params: PaginationParams): Promise<PageResponse<User>> {
  const response = await api.get<ApiResponse<PageResponse<User>>>("/api/v1/users", {
    params: buildParams(params),
  });
  return response.data.data;
}

export async function updateUserRole(id: number, role: Role): Promise<User> {
  const response = await api.patch<ApiResponse<User>>(`/api/v1/users/${id}/role`, { role });
  return response.data.data;
}
