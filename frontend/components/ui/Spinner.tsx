import styles from "./Spinner.module.css";

interface SpinnerProps {
  size?: "sm" | "lg";
  fullPage?: boolean;
  label?: string;
}

export function Spinner({ size = "sm", fullPage = false, label = "Loading..." }: SpinnerProps) {
  const spinner = (
    <span
      className={`${styles.spinner} ${size === "lg" ? styles["spinner-lg"] : ""}`}
      role="status"
      aria-label={label}
    />
  );

  if (fullPage) {
    return <div className={styles.fullPage}>{spinner}</div>;
  }

  return spinner;
}
