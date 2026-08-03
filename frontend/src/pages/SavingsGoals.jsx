import { useState } from 'react'
import { useSavingsGoals } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage } from '../components/Feedback'
import EmptyState from '../components/EmptyState'
import { formatCurrency } from '../utils/format'

// ============================================================
// TICKET-F090/F103 (Day 8-9, Sprint 7-8) — Savings Goals Page
// ============================================================
//
// WHAT: This page displays savings goals as cards with progress bars.
//       Each card shows: goal name, deadline, current/target amounts,
//       a visual progress bar, and a Contribute button.
//
// WHY:  Savings goals are a motivational feature — seeing progress toward
//       a goal (like "Holiday Fund: 60% complete") encourages saving.
//       The visual progress bar makes the abstract number tangible.
//
// ============================================================

export default function SavingsGoals() {
  // F090: fetch goals for the demo user (hardcoded userId=1, no auth yet)
  const { goals, loading, error, refetch } = useSavingsGoals(1)

  // -------------------------------------------------------
  // TODO TICKET-F103 (Day 9): Step 2 — Wire up Contribute button
  // -------------------------------------------------------
  // WHAT: Each goal card gets a "Contribute" button that opens an input field.
  //       The user enters an amount, and it's added to the goal's currentAmount.
  //
  // HOW:  1. Add state for the active goal: contributingId (which goal is being contributed to)
  //       2. Add state for the contribution amount: contributionAmount
  //       3. When "Contribute" is clicked:
  //          - Set contributingId to that goal's ID
  //          - Show a number input field and a "Submit" button
  //       4. When "Submit" is clicked:
  //          - Validate: amount must be > 0
  //          - Call PUT /api/goals/{id}/contribute with { amount: value }
  //            Use fetch with method: 'PUT', Content-Type: 'application/json'
  //          - On success: call refetch() to refresh the goals, clear contributingId
  //          - On error: show an error message
  //       5. Add a "Cancel" button to close the input without contributing
  //
  // WHY:  This demonstrates a business operation (not just CRUD).
  //       The frontend sends a partial update (just the amount), and the backend
  //       adds it to the existing value — this is different from a full PUT that
  //       replaces the entire resource.
  //
  // OBSERVE: Click Contribute on a goal with currentAmount = 500.
  //          Enter 100, click Submit. The progress bar should advance.
  //          The currentAmount should now show 600.
  //          Try entering 0 or a negative number — what happens?

  if (loading) return <Spinner />
  if (error) return <ErrorMessage message={error} />

  return (
    <div>
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Savings Goals</h1>

      {goals.length === 0 ? (
        <EmptyState title="No savings goals" body="Set a goal and start saving towards it." />
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
          gap: '1.2rem',
        }}>
          {goals.map(g => {
            const target = Number(g.targetAmount)
            const current = Number(g.currentAmount)
            const pct = target === 0 ? 0 : Math.min(100, Math.max(0, (current / target) * 100))

            return (
              <div key={g.goalId} className="card">
                <h3 style={{ marginBottom: '0.25rem' }}>{g.name}</h3>
                {g.deadline && (
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                    Target date: {g.deadline}
                  </p>
                )}
                <p style={{ marginBottom: '0.25rem' }}>
                  {formatCurrency(current)} of {formatCurrency(target)}
                </p>
                <div className="progress-bar-bg">
                  <div className="progress-bar-fill" style={{ width: `${pct}%` }} />
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  {pct.toFixed(0)}%{pct >= 100 ? ' (complete)' : ''}
                </p>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
