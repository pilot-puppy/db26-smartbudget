import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useTransactionData, useCategories } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage, Toast } from '../components/Feedback'
import EmptyState from '../components/EmptyState'
import { formatCurrency } from '../utils/format'

// ============================================================
// TICKET-F086/F087/F098/F102 (Day 8-9) — Transaction List Page
// ============================================================
//
// WHAT: This page displays all transactions in a sortable, filterable table.
//       It's the main data view of the application.
//
// WHY:  Users need to see their transaction history, search for specific entries,
//       and perform actions (edit, delete) on individual records.
//
// ============================================================

export default function TransactionList() {
  // F086: pull data via the F091 hook. Default to {} so the page renders
  // (empty table) instead of crashing while that hook is still a stub.
  const { transactions = [], loading, error, refetch } = useTransactionData() ?? {}
  const categories = useCategories()

  // TICKET-F095 — filter by transaction type (client-side, no re-fetch)
  const [typeFilter, setTypeFilter] = useState('ALL')
  // TICKET-F098 — filter by category name
  const [filterCategory, setFilterCategory] = useState('ALL')
  // TICKET-F096 — filter by date range (ISO strings compare with >= / <=)
  const [filterFrom, setFilterFrom] = useState('')
  const [filterTo, setFilterTo] = useState('')
  // TICKET-F097 — case-insensitive search on description
  const [searchTerm, setSearchTerm] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [editValues, setEditValues] = useState({})
  const [savingEdit, setSavingEdit] = useState(false)

  const filteredTransactions = useMemo(() => transactions
    .filter(t => typeFilter === 'ALL' || t.type === typeFilter)
    .filter(t => filterCategory === 'ALL' || t.category?.name === filterCategory)
    .filter(t => !filterFrom || t.txnDate >= filterFrom)
    .filter(t => !filterTo || t.txnDate <= filterTo)
    .filter(t => !searchTerm ||
      t.description?.toLowerCase().includes(searchTerm.toLowerCase())),
  [transactions, typeFilter, filterCategory, filterFrom, filterTo, searchTerm])

  // F104: toast feedback (green success / red error) for the delete action
  const [toast, setToast] = useState(null)

  function clearFilters() {
    setTypeFilter('ALL')
    setFilterCategory('ALL')
    setFilterFrom('')
    setFilterTo('')
    setSearchTerm('')
  }

  // F087: confirm, DELETE, then refetch so the row disappears without a reload.
  async function handleDelete(id) {
    if (!window.confirm('Delete this transaction?')) return
    const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' })
    if (res.ok) { refetch?.(); setToast({ type: 'success', message: 'Transaction deleted' }) }
    else setToast({ type: 'error', message: 'Could not delete transaction' })
  }

  function startEdit(transaction) {
    setEditingId(transaction.txnId)
    setEditValues({
      amount: String(transaction.amount),
      description: transaction.description ?? '',
      type: transaction.type,
    })
  }

  async function saveEdit(transaction) {
    const amount = Number(editValues.amount)
    if (!Number.isFinite(amount) || amount <= 0) {
      setToast({ type: 'error', message: 'Enter a positive amount' })
      return
    }

    setSavingEdit(true)
    try {
      const res = await fetch(`/api/transactions/${transaction.txnId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          amount,
          txnDate: transaction.txnDate,
          description: editValues.description,
          type: editValues.type,
        }),
      })
      if (!res.ok) {
        const body = await res.json().catch(() => null)
        throw new Error(body?.message || `HTTP ${res.status}`)
      }
      setEditingId(null)
      setEditValues({})
      await refetch?.()
      setToast({ type: 'success', message: 'Transaction updated' })
    } catch (err) {
      setToast({ type: 'error', message: err.message || 'Could not update transaction' })
    } finally {
      setSavingEdit(false)
    }
  }

  if (loading) return <Spinner />
  if (error) return <ErrorMessage message={error} />

  // -------------------------------------------------------
  // TODO TICKET-F098 (Day 9): Step 3 — Add filter bar
  // -------------------------------------------------------
  // WHAT: A filter section above the table with:
  //       - Category dropdown (filter by category)
  //       - Date range inputs (from date, to date)
  //       - Search input (filter by description keyword)
  //
  // HOW:  1. Add state variables for each filter: filterCategory, filterFrom, filterTo, searchTerm
  //       2. Use useMemo to create a "filteredTransactions" array that applies all filters
  //       3. Filter logic (inside useMemo):
  //          - If filterCategory is set, keep only transactions where category.name matches
  //          - If filterFrom is set, keep only transactions where txnDate >= filterFrom
  //          - If searchTerm is set, keep only transactions where description includes the term
  //       4. Render the table using filteredTransactions instead of transactions
  //       5. Render filter inputs above the table, each with onChange updating state
  //
  // WHY:  Filtering happens client-side (in the browser) because we already have all data.
  //       useMemo caches the filtered result so it only recalculates when filters or data change.
  //       This is faster than calling the API with filter parameters for every keystroke.
  //
  // OBSERVE: Type in the search box — the table should update instantly (no API calls).
  //          Select a category — only matching transactions should appear.

  // -------------------------------------------------------
  // TODO TICKET-F102 (Day 9): Step 4 — Add edit functionality
  // -------------------------------------------------------
  // WHAT: Each table row gets an "Edit" button that allows inline editing.
  //
  // HOW:  1. Add state for the currently editing transaction: editingId, editForm
  //       2. When Edit is clicked, set editingId to that row's ID
  //          and populate editForm with the current values
  //       3. In the table, if row ID === editingId, show input fields instead of text
  //       4. Add Save/Cancel buttons in the editing row
  //       5. On Save, call PUT /api/transactions/{id} with the updated data
  //       6. On success, call refetch() and clear editingId
  //
  // WHY:  Inline editing is a better UX than navigating to a separate edit page.
  //       The user sees the change immediately in context.
  //
  // OBSERVE: Click Edit → fields should become editable → change the amount →
  //          click Save → the row should update with the new value.

  const noData = transactions.length === 0
  const noMatches = !noData && filteredTransactions.length === 0

  return (
    <div>
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ color: 'var(--primary)' }}>Transactions</h1>
        <Link to="/add" className="btn btn-primary">+ Add Transaction</Link>
      </div>

      {/* F105: no data at all → onboarding empty state instead of an empty table */}
      {noData ? (
        <EmptyState
          title="No transactions yet"
          body="Start tracking your money — add your first transaction."
          ctaLabel="+ Add Transaction"
          ctaTo="/add" />
      ) : (
        <>
          {/* TICKET-F095 / F096 / F097 / F098 — filter bar */}
          <div className="card" style={{
            display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'flex-end',
            marginBottom: '1rem', padding: '1rem',
          }}>
            <div className="form-group" style={{ marginBottom: 0, minWidth: '140px' }}>
              <label htmlFor="type-filter">Type</label>
              <select id="type-filter" value={typeFilter}
                      onChange={e => setTypeFilter(e.target.value)}>
                <option value="ALL">All</option>
                <option value="INCOME">Income</option>
                <option value="EXPENSE">Expense</option>
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0, minWidth: '160px' }}>
              <label htmlFor="category-filter">Category</label>
              <select id="category-filter" value={filterCategory}
                      onChange={e => setFilterCategory(e.target.value)}>
                <option value="ALL">All categories</option>
                {categories.map(c => (
                  <option key={c.categoryId} value={c.name}>{c.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="filter-from">From</label>
              <input id="filter-from" type="date" value={filterFrom}
                     onChange={e => setFilterFrom(e.target.value)} />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="filter-to">To</label>
              <input id="filter-to" type="date" value={filterTo}
                     onChange={e => setFilterTo(e.target.value)} />
            </div>

            <div className="form-group" style={{ marginBottom: 0, flex: '1 1 200px' }}>
              <label htmlFor="search-term">Search</label>
              <input id="search-term" type="search" placeholder="Search transactions..."
                     value={searchTerm} onChange={e => setSearchTerm(e.target.value)} />
            </div>

            <button type="button" className="btn btn-secondary" onClick={clearFilters}>
              Clear filters
            </button>

            <span style={{ alignSelf: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Showing {filteredTransactions.length} of {transactions.length}
            </span>
          </div>

          {/* F105: filters matched nothing → styled empty state with a clear-filters action */}
          {noMatches ? (
            <EmptyState
              title="No matches"
              body="No transactions match your current filters."
              ctaLabel="Clear filters"
              onAction={clearFilters} />
          ) : (
            <div className="card">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Date</th>
                    <th>Category</th>
                    <th>Description</th>
                    <th>Amount</th>
                    <th>Type</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.map(t => editingId === t.txnId ? (
                    <tr key={t.txnId}>
                      <td>{t.txnId}</td>
                      <td>{t.txnDate}</td>
                      <td>{t.category?.name}</td>
                      <td>
                        <input value={editValues.description}
                               aria-label={`Description for transaction ${t.txnId}`}
                               onChange={e => setEditValues(v => ({ ...v, description: e.target.value }))} />
                      </td>
                      <td>
                        <input type="number" step="0.01" min="0.01" value={editValues.amount}
                               aria-label={`Amount for transaction ${t.txnId}`}
                               onChange={e => setEditValues(v => ({ ...v, amount: e.target.value }))} />
                      </td>
                      <td>
                        <select value={editValues.type}
                                aria-label={`Type for transaction ${t.txnId}`}
                                onChange={e => setEditValues(v => ({ ...v, type: e.target.value }))}>
                          <option value="INCOME">INCOME</option>
                          <option value="EXPENSE">EXPENSE</option>
                        </select>
                      </td>
                      <td style={{ display: 'flex', gap: '0.5rem' }}>
                        <button className="btn btn-success" disabled={savingEdit}
                                aria-label={`Save transaction ${t.txnId}`}
                                title="Save transaction"
                                onClick={() => saveEdit(t)}>{savingEdit ? 'Saving...' : 'Save'}</button>
                        <button className="btn btn-secondary" disabled={savingEdit}
                                aria-label={`Cancel editing transaction ${t.txnId}`}
                                title="Cancel editing"
                                onClick={() => { setEditingId(null); setEditValues({}) }}>Cancel</button>
                      </td>
                    </tr>
                  ) : (
                    <tr key={t.txnId}>
                      <td>{t.txnId}</td>
                      <td>{t.txnDate}</td>
                      <td>{t.category?.name}</td>
                      <td>{t.description}</td>
                      <td style={{ color: t.type === 'INCOME' ? 'var(--success)' : 'var(--danger)' }}>
                        {formatCurrency(t.amount)}
                      </td>
                      <td><span className={`badge badge--${t.type.toLowerCase()}`}>{t.type}</span></td>
                      <td style={{ display: 'flex', gap: '0.5rem' }}>
                        <button className="btn btn-secondary"
                                aria-label={`Edit transaction ${t.txnId}`}
                                title="Edit transaction"
                                onClick={() => startEdit(t)}>Edit</button>
                        <button className="btn btn-danger"
                                aria-label={`Delete transaction ${t.txnId}`}
                                title="Delete transaction"
                                onClick={() => handleDelete(t.txnId)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  )
}
