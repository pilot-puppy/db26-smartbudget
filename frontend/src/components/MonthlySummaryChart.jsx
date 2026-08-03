import { useMemo } from 'react'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer,
} from 'recharts'

// ============================================================
// TICKET-F100 (Day 9, Sprint 8) — Monthly Summary Bar Chart
// ============================================================
//
// WHAT: A bar chart that shows income vs. expenses for each month.
//       Uses the Recharts library — a React wrapper around D3.js.
//       The component receives a "transactions" array as a prop and
//       aggregates it into monthly totals before rendering.
//
// WHY:  Visual charts help users spot trends. A table shows raw data,
//       but a chart reveals patterns: "I spend more in December" or
//       "My income increased over the last 3 months."
//
// KEY CONCEPTS:
//   Props:     Data passed FROM a parent component TO this child component
//   useMemo:   Caches expensive calculations (aggregation) to avoid re-computing on every render
//   Recharts:  React charting library with components like <BarChart>, <Bar>, <XAxis>, <YAxis>
//
// ============================================================

export default function MonthlySummaryChart({ transactions = [] }) {

  // Step 1 — aggregate transactions into one row per month: { month, income, expense }
  const data = useMemo(() => {
    const monthMap = {}
    for (const t of transactions) {
      const month = (t.txnDate ?? '').substring(0, 7) // "2026-05-15" -> "2026-05"
      if (!month) continue
      if (!monthMap[month]) monthMap[month] = { month, income: 0, expense: 0 }
      const amount = Number(t.amount) || 0
      if (t.type === 'INCOME') monthMap[month].income += amount
      if (t.type === 'EXPENSE') monthMap[month].expense += amount
    }
    return Object.values(monthMap)
      .sort((a, b) => a.month.localeCompare(b.month))
      .map(d => ({
        ...d,
        income: Math.round(d.income * 100) / 100,
        expense: Math.round(d.expense * 100) / 100,
      }))
  }, [transactions])

  if (data.length === 0) {
    return <p style={{ color: 'var(--text-muted)' }}>No data to chart yet.</p>
  }

  // Step 2 — render the bar chart (green income bar, red expense bar, per month)
  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data} margin={{ top: 16, right: 24, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="month" />
        <YAxis tickFormatter={v => '£' + v} />
        <Tooltip formatter={(value, name) => ['£' + Number(value).toFixed(2), name]} />
        <Legend />
        <Bar dataKey="income" fill="#2e7d32" name="Income" />
        <Bar dataKey="expense" fill="#c62828" name="Expense" />
      </BarChart>
    </ResponsiveContainer>
  )
}
