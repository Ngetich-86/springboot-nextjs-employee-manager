"use client";

import { FiLogOut, FiMenu } from "react-icons/fi";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { useAuthStore } from "@/store/authStore";
import { RoleBadge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import styles from "./Navbar.module.css";

interface NavbarProps {
  onMenuClick: () => void;
}

export function Navbar({ onMenuClick }: NavbarProps) {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const clearAuth = useAuthStore((state) => state.clearAuth);

  const handleLogout = () => {
    clearAuth();
    toast.info("You have been logged out.");
    router.push("/login");
  };

  return (
    <header className={styles.navbar}>
      <div className={styles.left}>
        <button
          type="button"
          className={styles.menuButton}
          onClick={onMenuClick}
          aria-label="Toggle menu"
        >
          <FiMenu size={18} />
        </button>
        <span className={styles.brand}>Pesira HR</span>
      </div>
      <div className={styles.right}>
        {user ? (
          <div className={styles.userInfo}>
            <span className={styles.email}>{user.email}</span>
            <RoleBadge role={user.role} />
          </div>
        ) : null}
        <Button variant="ghost" size="sm" onClick={handleLogout}>
          <FiLogOut size={16} />
          Logout
        </Button>
      </div>
    </header>
  );
}
