import { Metadata } from "next";
import { redirect } from "next/navigation";

export const metadata: Metadata = {
  title: "Pesira HR",
};

export default function HomePage() {
  redirect("/dashboard");
}
