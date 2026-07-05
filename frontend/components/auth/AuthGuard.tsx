"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { useAuthHydration } from "@/hooks/useHydration";
import { Spinner } from "@/components/ui/Spinner";
import { DashboardShell } from "@/components/layout/DashboardShell";

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const hydrated = useAuthHydration();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  useEffect(() => {
    if (hydrated && !isAuthenticated()) {
      router.replace("/login");
    }
  }, [hydrated, isAuthenticated, router]);

  if (!hydrated || !isAuthenticated()) {
    return <Spinner fullPage />;
  }

  return <DashboardShell>{children}</DashboardShell>;
}

export function GuestGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const hydrated = useAuthHydration();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  useEffect(() => {
    if (hydrated && isAuthenticated()) {
      router.replace("/dashboard");
    }
  }, [hydrated, isAuthenticated, router]);

  if (!hydrated) {
    return <Spinner fullPage />;
  }

  if (isAuthenticated()) {
    return <Spinner fullPage />;
  }

  return <>{children}</>;
}

export function AdminGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const isAdmin = useAuthStore((state) => state.isAdmin);

  useEffect(() => {
    if (!isAdmin()) {
      router.replace("/dashboard");
    }
  }, [isAdmin, router]);

  if (!isAdmin()) {
    return <Spinner fullPage />;
  }

  return <>{children}</>;
}
