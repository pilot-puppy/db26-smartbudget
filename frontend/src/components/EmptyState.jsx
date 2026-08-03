import { Link } from 'react-router-dom'

// ============================================================
// TICKET-F105 (Day 9) — Reusable empty-state card
// ============================================================
// A blank list reads as "broken"; this turns it into onboarding.
// Pass a Link CTA (ctaTo) or a button action (onAction) — or neither.

export default function EmptyState({ title, body, ctaLabel, ctaTo, onAction }) {
  return (
    <div className="card" style={{ textAlign: 'center', padding: '3rem', maxWidth: 480, margin: '2rem auto' }}>
      <h3 style={{ marginBottom: '0.5rem', color: 'var(--primary)' }}>{title}</h3>
      <p style={{ color: 'var(--text-muted)', marginBottom: ctaLabel ? '1.25rem' : 0 }}>{body}</p>
      {ctaLabel && ctaTo && <Link to={ctaTo} className="btn btn-primary">{ctaLabel}</Link>}
      {ctaLabel && onAction && (
        <button type="button" className="btn btn-secondary" onClick={onAction}>{ctaLabel}</button>
      )}
    </div>
  )
}
