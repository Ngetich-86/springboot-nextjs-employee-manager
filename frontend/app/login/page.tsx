import { Metadata } from "next";
import { GuestGuard } from "@/components/auth/AuthGuard";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { AuthForm } from "@/components/auth/AuthForm";

export const metadata: Metadata = {
  title: "Login | Pesira HR",
};

export default function LoginPage() {
  return (
    <GuestGuard>
      <AuthLayout
        title="Welcome back"
        subtitle="Sign in to your account to continue"
        footerText="Don't have an account?"
        footerLinkText="Register"
        footerHref="/register"
      >
        <AuthForm mode="login" />
      </AuthLayout>
    </GuestGuard>
  );
}
