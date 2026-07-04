import { formatCurrency, formatDate } from "./formatters";

describe("formatters", () => {
  it("formats currency values", () => {
    expect(formatCurrency(75000)).toBe("$75,000.00");
  });

  it("formats ISO date strings", () => {
    expect(formatDate("2024-01-15")).toMatch(/Jan/i);
  });
});
