import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'
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

  // TICKET-F095 — filter by transaction type (client-side, no re-fetch)
  const [typeFilter, setTypeFilter] = useState('ALL')
  // TICKET-F096 — filter by date range (ISO strings compare with >= / <=)
  const [filterFrom, setFilterFrom] = useState('')
  const [filterTo, setFilterTo]     = useState('')
  // TICKET-F097 — case-insensitive search on description
  const [searchTerm, setSearchTerm] = useState('')

  const filteredTransactions = useMemo(() => transactions
    .filter(t => typeFilter === 'ALL' || t.type === typeFilter)
    .filter(t => !filterFrom || t.txnDate >= filterFrom)
    .filter(t => !filterTo || t.txnDate <= filterTo)
    .filter(t => !searchTerm ||
      t.description?.toLowerCase().includes(searchTerm.toLowerCase())),
  [transactions, typeFilter, filterFrom, filterTo, searchTerm])

  // F104: toast feedback (green success / red error) for the delete action
  const [toast, setToast] = useState(null)

  // TICKET-F102 — inline edit transaction state
  const [editingId, setEditingId] = useState(null)
  const [editForm, setEditForm]   = useState({ amount: '', description: '', type: 'EXPENSE', txnDate: '' })

  function clearFilters() {
    setTypeFilter('ALL'); setFilterFrom(''); setFilterTo(''); setSearchTerm('')
  }

  function handleStartEdit(t) {
    setEditingId(t.txnId)
    setEditForm({
      amount: String(t.amount),
      description: t.description ?? '',
      type: t.type,
      txnDate: t.txnDate,
    })
  }

  function handleCancelEdit() {
    setEditingId(null)
  }

  async function handleSaveEdit(t) {
    try {
      const res = await fetch(`/api/transactions/${t.txnId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          amount: parseFloat(editForm.amount),
          txnDate: editForm.txnDate || t.txnDate,
          description: editForm.description,
          type: editForm.type,
        }),
      })
      if (!res.ok) {
        const body = await res.json().catch(() => null)
        throw new Error(body?.message || `HTTP ${res.status}`)
      }
      setEditingId(null)
      refetch?.()
      setToast({ type: 'success', message: 'Transaction updated successfully' })
    } catch (err) {
      setToast({ type: 'error', message: err.message || 'Could not update transaction' })
    }
  }

  // F087: confirm, DELETE, then refetch so the row disappears without a reload.
  async function handleDelete(id) {
    if (!window.confirm('Delete this transaction?')) return
    const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' })
    if (res.ok) { refetch?.(); setToast({ type: 'success', message: 'Transaction deleted' }) }
    else setToast({ type: 'error', message: 'Could not delete transaction' })
  }

  if (loading) return <Spinner />
  if (error) return <ErrorMessage message={error} />

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
                  {filteredTransactions.map(t => (
                    editingId === t.txnId ? (
                      <tr key={t.txnId}>
                        <td>{t.txnId}</td>
                        <td>
                          <input type="date" value={editForm.txnDate}
                                 onChange={e => setEditForm(v => ({ ...v, txnDate: e.target.value }))}
                                 style={{ padding: '0.3rem 0.5rem', border: '1px solid var(--border)', borderRadius: '4px' }} />
                        </td>
                        <td>{t.category?.name}</td>
                        <td>
                          <input type="text" value={editForm.description}
                                 onChange={e => setEditForm(v => ({ ...v, description: e.target.value }))}
                                 style={{ padding: '0.3rem 0.5rem', border: '1px solid var(--border)', borderRadius: '4px', width: '100%' }} />
                        </td>
                        <td>
                          <input type="number" step="0.01" value={editForm.amount}
                                 onChange={e => setEditForm(v => ({ ...v, amount: e.target.value }))}
                                 style={{ padding: '0.3rem 0.5rem', border: '1px solid var(--border)', borderRadius: '4px', width: '90px' }} />
                        </td>
                        <td>
                          <select value={editForm.type}
                                  onChange={e => setEditForm(v => ({ ...v, type: e.target.value }))}
                                  style={{ padding: '0.3rem 0.5rem', border: '1px solid var(--border)', borderRadius: '4px' }}>
                            <option value="INCOME">INCOME</option>
                            <option value="EXPENSE">EXPENSE</option>
                          </select>
                        </td>
                        <td style={{ whiteSpace: 'nowrap' }}>
                          <button className="btn btn-primary btn-sm"
                                  style={{ marginRight: '0.4rem', padding: '0.3rem 0.7rem' }}
                                  onClick={() => handleSaveEdit(t)}>Save</button>
                          <button className="btn btn-secondary btn-sm"
                                  style={{ padding: '0.3rem 0.7rem' }}
                                  onClick={handleCancelEdit}>Cancel</button>
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
                        <td style={{ whiteSpace: 'nowrap' }}>
                          <button className="btn btn-secondary btn-sm"
                                  style={{ marginRight: '0.4rem', padding: '0.3rem 0.7rem' }}
                                  aria-label={`Edit transaction ${t.txnId}`}
                                  title="Edit transaction"
                                  onClick={() => handleStartEdit(t)}>Edit</button>
                          <button className="btn btn-danger btn-sm"
                                  style={{ padding: '0.3rem 0.7rem' }}
                                  aria-label={`Delete transaction ${t.txnId}`}
                                  title="Delete transaction"
                                  onClick={() => handleDelete(t.txnId)}>Delete</button>
                        </td>
                      </tr>
                    )
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
