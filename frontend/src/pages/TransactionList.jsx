import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage } from '../components/Feedback'

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

  // TICKET-F095 — filter by transaction type (client-side, no re-fetch)
  const [typeFilter, setTypeFilter] = useState('ALL')
  // TICKET-F096 — filter by date range (ISO strings compare with >= / <=)
  const [filterFrom, setFilterFrom] = useState('')
  const [filterTo, setFilterTo] = useState('')
  // TICKET-F097 — case-insensitive search on description
  const [searchTerm, setSearchTerm] = useState('')

  const filteredTransactions = useMemo(() => transactions
    .filter(t => typeFilter === 'ALL' || t.type === typeFilter)
    .filter(t => !filterFrom || t.txnDate >= filterFrom)
    .filter(t => !filterTo || t.txnDate <= filterTo)
    .filter(t => !searchTerm ||
      t.description?.toLowerCase().includes(searchTerm.toLowerCase())),
  [transactions, typeFilter, filterFrom, filterTo, searchTerm])

  // F087: confirm, DELETE, then refetch so the row disappears without a reload.
  async function handleDelete(id) {
    if (!window.confirm('Delete this transaction?')) return
    const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' })
    if (res.ok) refetch?.()
    else alert('Could not delete transaction.')
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

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ color: 'var(--primary)' }}>Transactions</h1>
        <Link to="/add" className="btn btn-primary">+ Add Transaction</Link>
      </div>

      {/* TICKET-F095 / F096 / F097 — filter bar (F098 category filter still TODO below) */}
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

        <button type="button" className="btn btn-secondary"
                onClick={() => { setTypeFilter('ALL'); setFilterFrom(''); setFilterTo(''); setSearchTerm('') }}>
          Clear filters
        </button>

        <span style={{ alignSelf: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Showing {filteredTransactions.length} of {transactions.length}
        </span>
      </div>

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
            {transactions.length === 0 ? (
              <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No transactions yet.</td></tr>
            ) : filteredTransactions.length === 0 ? (
              <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No transactions match this filter.</td></tr>
            ) : filteredTransactions.map(t => (
              <tr key={t.txnId}>
                <td>{t.txnId}</td>
                <td>{t.txnDate}</td>
                <td>{t.category?.name}</td>
                <td>{t.description}</td>
                <td style={{ color: t.type === 'INCOME' ? 'var(--success)' : 'var(--danger)' }}>
                  £{Number(t.amount).toFixed(2)}
                </td>
                <td><span className={`badge badge--${t.type.toLowerCase()}`}>{t.type}</span></td>
                <td>
                  <button className="btn btn-danger" onClick={() => handleDelete(t.txnId)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
