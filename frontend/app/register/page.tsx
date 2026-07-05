import { Metadata } from "next";
import { GuestGuard } from "@/components/auth/AuthGuard";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { AuthForm } from "@/components/auth/AuthForm";

export const metadata: Metadata = {
  title: "Register | Pesira HR",
};

export default function RegisterPage() {
  return (
    <GuestGuard>
      <AuthLayout
        title="Create account"
        subtitle="Register to access the employee management system"
        footerText="Already have an account?"
        footerLinkText="Sign in"
        footerHref="/login"
      >
        <AuthForm mode="register" />
      </AuthLayout>
    </GuestGuard>
  );
}
