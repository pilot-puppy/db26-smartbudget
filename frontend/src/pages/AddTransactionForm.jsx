import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCategories } from '../hooks/useBudgetAPI'

// ============================================================
// TICKET-F088/F089 (Day 8, Sprint 7) — Add Transaction Form
// ============================================================
//
// WHAT: A form page that allows users to create new transactions.
//       Uses React "controlled components" — the form state lives in React,
//       not in the DOM. Every input change updates React state, and React
//       re-renders the input with the new value.
//
// WHY:  Controlled components give you full control over form data.
//       You can validate before submission, format values, and prevent
//       invalid characters — all in JavaScript, before hitting the server.
//
// ============================================================

export default function AddTransactionForm() {
  const navigate = useNavigate()

  // F088: one state object holds every field; one handleChange updates it by name.
  const [form, setForm] = useState({
    categoryId: '',
    amount: '',
    txnDate: new Date().toISOString().substring(0, 10),
    description: '',
  })
  const [error, setError] = useState(null)

  function handleChange(e) {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  // F091 populates this dropdown; default to [] so the form works before that lands.
  const categories = useCategories() ?? []

  // F089: validate client-side, POST the payload, then redirect to the list on success.
  async function handleSubmit(e) {
    e.preventDefault()
    const category = categories.find(c => String(c.categoryId) === String(form.categoryId))
    const today = new Date().toISOString().substring(0, 10)

    if (!form.categoryId) return setError('Please choose a category.')
    if (!(parseFloat(form.amount) > 0)) return setError('Amount must be greater than zero.')
    if (form.txnDate > today) return setError('Date cannot be in the future.')

    const body = {
      user: { userId: 1 },
      category: { categoryId: Number(form.categoryId) },
      amount: parseFloat(form.amount),
      txnDate: form.txnDate,
      description: form.description,
      type: category?.type,
    }

    const res = await fetch('/api/transactions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (res.ok) navigate('/transactions')
    else setError('Could not save transaction. Please try again.')
  }

  return (
    <div>
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Add Transaction</h1>

      <form className="card" style={{ maxWidth: 540 }} onSubmit={handleSubmit} noValidate>
        {error && <p style={{ color: 'var(--danger)', marginBottom: '1rem' }}>{error}</p>}

        <div className="form-group">
          <label htmlFor="categoryId">Category</label>
          <select id="categoryId" name="categoryId" value={form.categoryId} onChange={handleChange}>
            <option value="">-- Select category --</option>
            {categories.map(c => (
              <option key={c.categoryId} value={c.categoryId}>{c.name} ({c.type})</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="amount">Amount (£)</label>
          <input id="amount" name="amount" type="number" step="0.01" min="0.01"
                 value={form.amount} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label htmlFor="txnDate">Date</label>
          <input id="txnDate" name="txnDate" type="date" value={form.txnDate} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <input id="description" name="description" type="text" maxLength={200}
                 value={form.description} onChange={handleChange} />
        </div>

        <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
          <button type="submit" className="btn btn-primary">Add Transaction</button>
          <Link to="/transactions" className="btn btn-secondary">Back to Transactions</Link>
        </div>
      </form>
    </div>
  )
}
