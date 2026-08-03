// ============================================================
// TICKET-F106 (Day 9) — Currency formatting helper
// ============================================================
// One place that turns a number into "£3,500.00" so every screen agrees
// on symbol, decimals, and thousands separators. null/undefined/NaN → £0.00.

const GBP = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' })

export function formatCurrency(amount) {
  const n = Number(amount)
  return GBP.format(Number.isFinite(n) ? n : 0)
}
