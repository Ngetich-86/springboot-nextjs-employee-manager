"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { loginSchema, registerSchema } from "@/lib/validators";
import type { LoginFormValues, RegisterFormValues } from "@/lib/validators";
import { login, register } from "@/services/authService";
import { getErrorMessage } from "@/services/api";
import { useAuthStore } from "@/store/authStore";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import styles from "./AuthLayout.module.css";

interface AuthFormProps {
  mode: "login" | "register";
}

export function AuthForm({ mode }: AuthFormProps) {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);
  const isLogin = mode === "login";

  const {
    register: registerField,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues | RegisterFormValues>({
    resolver: zodResolver(isLogin ? loginSchema : registerSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const mutation = useMutation({
    mutationFn: isLogin ? login : register,
    onSuccess: (data) => {
      setAuth(data.accessToken, data.user);
      toast.success(isLogin ? "Welcome back!" : "Account created successfully!");
      router.push("/dashboard");
    },
    onError: (error) => {
      toast.error(getErrorMessage(error, isLogin ? "Login failed" : "Registration failed"));
    },
  });

  return (
    <form
      className={styles.form}
      onSubmit={handleSubmit((values) => mutation.mutate(values))}
      noValidate
    >
      <Input
        label="Email"
        type="email"
        autoComplete="email"
        error={errors.email?.message}
        {...registerField("email")}
      />
      <Input
        label="Password"
        type="password"
        autoComplete={isLogin ? "current-password" : "new-password"}
        error={errors.password?.message}
        hint={!isLogin ? "Minimum 6 characters" : undefined}
        {...registerField("password")}
      />
      <Button type="submit" fullWidth disabled={mutation.isPending}>
        {mutation.isPending ? <Spinner /> : isLogin ? "Sign in" : "Create account"}
      </Button>
    </form>
  );
}
