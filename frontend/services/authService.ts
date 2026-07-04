import { api } from "./api";
import { ApiResponse, AuthResponse, LoginFormValues, RegisterFormValues, User } from "@/types";

export async function login(credentials: LoginFormValues): Promise<AuthResponse> {
  const response = await api.post<ApiResponse<AuthResponse>>("/api/v1/auth/login", credentials);
  return response.data.data;
}

export async function register(credentials: RegisterFormValues): Promise<AuthResponse> {
  const response = await api.post<ApiResponse<AuthResponse>>("/api/v1/auth/register", credentials);
  return response.data.data;
}

export async function getCurrentUser(): Promise<User> {
  const response = await api.get<ApiResponse<User>>("/api/v1/auth/me");
  return response.data.data;
}
