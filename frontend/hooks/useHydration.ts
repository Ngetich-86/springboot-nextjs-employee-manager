"use client";

import { useEffect, useState } from "react";
import { useAuthStore } from "@/store/authStore";

export function useAuthHydration(): boolean {
  const [hydrated, setHydrated] = useState(() => useAuthStore.persist.hasHydrated());

  useEffect(() => {
    if (hydrated) {
      return undefined;
    }

    return useAuthStore.persist.onFinishHydration(() => {
      setHydrated(true);
    });
  }, [hydrated]);

  return hydrated;
}
