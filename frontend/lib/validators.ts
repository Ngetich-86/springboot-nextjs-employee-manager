import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});

export const registerSchema = z.object({
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
  password: z
    .string()
    .min(6, "Password must be at least 6 characters"),
});

export const employeeSchema = z.object({
  firstName: z.string().min(1, "First name is required"),
  lastName: z.string().min(1, "Last name is required"),
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
  department: z.string().min(1, "Department is required"),
  position: z.string().min(1, "Position is required"),
  salary: z.coerce.number().positive("Salary must be greater than zero"),
  hireDate: z.string().min(1, "Hire date is required"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;
export type EmployeeFormSchema = z.infer<typeof employeeSchema>;
