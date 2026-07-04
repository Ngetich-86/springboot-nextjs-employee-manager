"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import { Role, User } from "@/types";

interface AuthState {
  accessToken: string | null;
  user: User | null;
  setAuth: (accessToken: string, user: User) => void;
  setUser: (user: User) => void;
  clearAuth: () => void;
  isAuthenticated: () => boolean;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      setAuth: (accessToken, user) => set({ accessToken, user }),
      setUser: (user) => set({ user }),
      clearAuth: () => set({ accessToken: null, user: null }),
      isAuthenticated: () => Boolean(get().accessToken),
      isAdmin: () => get().user?.role === Role.ADMIN,
    }),
    {
      name: "pesira-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        user: state.user,
      }),
    },
  ),
);
