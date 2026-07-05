"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FiHome, FiUsers, FiUserCheck } from "react-icons/fi";
import { useAuthStore } from "@/store/authStore";
import styles from "./Sidebar.module.css";

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: FiHome, adminOnly: false },
  { href: "/dashboard/employees", label: "Employees", icon: FiUsers, adminOnly: false },
  { href: "/dashboard/users", label: "Users", icon: FiUserCheck, adminOnly: true },
];

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const pathname = usePathname();
  const isAdmin = useAuthStore((state) => state.isAdmin());

  const visibleItems = navItems.filter((item) => !item.adminOnly || isAdmin);

  return (
    <>
      {isOpen ? <div className={styles.overlay} onClick={onClose} role="presentation" /> : null}
      <aside className={`${styles.sidebar} ${isOpen ? styles.open : ""}`}>
        <nav className={styles.nav}>
          <span className={styles.sectionLabel}>Main Menu</span>
          {visibleItems.map((item) => {
            const Icon = item.icon;
            const isActive =
              pathname === item.href ||
              (item.href !== "/dashboard" && pathname.startsWith(item.href));

            return (
              <Link
                key={item.href}
                href={item.href}
                className={`${styles.link} ${isActive ? styles.active : ""}`}
                onClick={onClose}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>
    </>
  );
}
