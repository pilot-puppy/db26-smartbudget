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
  const { goals = [], loading, error, refetch } = useSavingsGoals(1)

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
          gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
          gap: '1.2rem',
        }}>
          {goals.map(g => (
            <GoalCard key={g.goalId} goal={g} onContribute={handleContribute} />
          ))}
        </div>
      )}
    </div>
  )
}

function GoalCard({ goal, onContribute }) {
  const target = Number(goal.targetAmount)
  const current = Number(goal.currentAmount)
  const pct = Math.min(100, Math.max(0, target ? (current / target) * 100 : 0))
  const colour = pct < 33 ? '#c62828' : pct < 66 ? '#f9a825' : '#2e7d32'

  const [isContributing, setIsContributing] = useState(false)
  const [amount, setAmount] = useState('')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState(null)

  async function submit(e) {
    e.preventDefault()
    const value = parseFloat(amount)
    if (!Number.isFinite(value) || value <= 0) {
      setErr('Enter a positive amount')
      return
    }
    setBusy(true)
    setErr(null)
    try {
      await onContribute(goal.goalId, value)
      setAmount('')
      setIsContributing(false)
    } catch (e) {
      setErr(e.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="card">
      <h3 style={{ marginBottom: '0.25rem' }}>{goal.name}</h3>
      {goal.deadline && (
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
          Target date: {goal.deadline}
        </p>
      )}
      <p style={{ marginBottom: '0.25rem' }}>
        {formatCurrency(current)} of {formatCurrency(target)}
      </p>
      <div className="progress-bar-bg">
        <div className="progress-bar-fill" style={{ width: `${pct}%`, background: colour }} />
      </div>
      <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
        {pct.toFixed(0)}%{pct >= 100 ? ' (complete)' : ''}
      </p>

      {!isContributing ? (
        <button
          className="btn btn-primary"
          style={{ marginTop: '1rem', width: '100%' }}
          onClick={() => { setIsContributing(true); setErr(null); }}>
          Contribute
        </button>
      ) : (
        <form onSubmit={submit} style={{ marginTop: '1rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <input
              type="number"
              step="0.01"
              min="0.01"
              placeholder="Amount..."
              value={amount}
              onChange={e => setAmount(e.target.value)}
              disabled={busy}
              style={{ flex: 1, padding: '0.4rem 0.6rem', border: '1px solid var(--border)', borderRadius: '4px' }}
            />
            <button
              type="submit"
              disabled={busy || !amount}
              className="btn btn-primary"
              style={{ padding: '0.4rem 0.8rem' }}>
              {busy ? '...' : 'Submit'}
            </button>
            <button
              type="button"
              disabled={busy}
              className="btn btn-secondary"
              style={{ padding: '0.4rem 0.8rem' }}
              onClick={() => { setIsContributing(false); setErr(null); setAmount(''); }}>
              Cancel
            </button>
          </div>
          {err && <p style={{ color: 'var(--danger)', marginTop: '0.5rem', fontSize: '0.85rem' }}>{err}</p>}
        </form>
      )}
    </div>
  )
}
