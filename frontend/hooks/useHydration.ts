"use client";

import { useSyncExternalStore } from "react";
import { useAuthStore } from "@/store/authStore";

function subscribeToAuthHydration(onStoreChange: () => void): () => void {
  const persist = useAuthStore.persist;

  if (!persist?.onFinishHydration) {
    return () => {};
  }

  return persist.onFinishHydration(onStoreChange);
}

function getAuthHydrationSnapshot(): boolean {
  const persist = useAuthStore.persist;
  return persist?.hasHydrated?.() ?? true;
}

function getAuthHydrationServerSnapshot(): boolean {
  return false;
}

export function useAuthHydration(): boolean {
  return useSyncExternalStore(
    subscribeToAuthHydration,
    getAuthHydrationSnapshot,
    getAuthHydrationServerSnapshot,
  );
}
