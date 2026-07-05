"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { employeeSchema, type EmployeeFormSchema } from "@/lib/validators";
import { EmployeeFormValues } from "@/types";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import styles from "./EmployeeForm.module.css";

interface EmployeeFormProps {
  defaultValues?: EmployeeFormValues;
  onSubmit: (values: EmployeeFormValues) => void;
  onCancel: () => void;
  isSubmitting?: boolean;
  submitLabel?: string;
}

export function EmployeeForm({
  defaultValues,
  onSubmit,
  onCancel,
  isSubmitting = false,
  submitLabel = "Save Employee",
}: EmployeeFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EmployeeFormSchema>({
    resolver: zodResolver(employeeSchema),
    defaultValues: defaultValues ?? {
      firstName: "",
      lastName: "",
      email: "",
      department: "",
      position: "",
      salary: 0,
      hireDate: "",
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className={styles.form}>
        <Input label="First Name" error={errors.firstName?.message} {...register("firstName")} />
        <Input label="Last Name" error={errors.lastName?.message} {...register("lastName")} />
        <Input
          label="Email"
          type="email"
          error={errors.email?.message}
          {...register("email")}
        />
        <Input
          label="Department"
          error={errors.department?.message}
          {...register("department")}
        />
        <Input label="Position" error={errors.position?.message} {...register("position")} />
        <Input
          label="Salary"
          type="number"
          step="0.01"
          min="0"
          error={errors.salary?.message}
          {...register("salary")}
        />
        <Input
          label="Hire Date"
          type="date"
          className={styles.fullWidth}
          error={errors.hireDate?.message}
          {...register("hireDate")}
        />
      </div>
      <div className={styles.actions}>
        <Button type="button" variant="secondary" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? <Spinner /> : submitLabel}
        </Button>
      </div>
    </form>
  );
}
