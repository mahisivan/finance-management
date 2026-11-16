import React, { useEffect, useState } from "react";
import {
  NavLink,
  Navigate,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { BarChart, Bar, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import api from "./api";
import PaginatedExportTable from "./components/PaginatedExportTable";

const icons = {
  companies: "bi-buildings",
  designations: "bi-person-badge",
  accounts: "bi-bank",
  salaries: "bi-cash-stack",
  loans: "bi-credit-card-2-front",
  transactions: "bi-arrow-left-right",
  "credit-card-transactions": "bi-credit-card",
};
const money = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
const plainAmount = (value) => Number(value || 0).toFixed(2);
const title = (value) =>
  value
    .replaceAll("-", " ")
    .replaceAll("_", " ")
    .replace(/\b\w/g, (character) => character.toUpperCase());
const accountTypeLabel = (type) =>
  ({ S: "Saving", C: "Current", L: "Loan" })[
    String(type || "")
      .trim()
      .toUpperCase()
  ] || type;
const statusResources = ["companies", "designations"];
const menuOnlySections = ["salaries", "transactions", "credit-card-transactions", "loans"];
const transactionResources = ["transactions", "credit-card-transactions"];
const dateResources = [
  "salaries",
  "loans",
  "transactions",
  "credit-card-transactions",
];
const todayDate = () => {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 10);
};

function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const submit = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const response = await api.post("/auth/login", { email, password });
      localStorage.setItem("fna_token", response.data.access_token);
      localStorage.setItem("fna_user", JSON.stringify(response.data.user));
      onLogin(response.data.user);
    } catch (requestError) {
      setError(requestError.response?.data?.detail || "Unable to sign in");
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="login-page">
      <div className="login-card">
        <div className="brand-mark login-mark">
          <i className="bi bi-wallet2" />
        </div>
        <div className="eyebrow mt-4">PERSONAL FINANCE WORKSPACE</div>
        <h1>Welcome back</h1>
        <p>Sign in to view your financial dashboard.</p>
        {error && <div className="alert alert-danger py-2">{error}</div>}
        <form onSubmit={submit}>
          <label className="form-label">Email address</label>
          <input
            className="form-control mb-3"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            autoComplete="username"
          />
          <label className="form-label">Password</label>
          <input
            className="form-control mb-4"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            autoComplete="current-password"
          />
          <button className="btn btn-primary w-100 py-2" disabled={busy}>
            {busy ? "Signing in..." : "Sign in"}
          </button>
        </form>
        <small className="text-muted d-block mt-4">
          Sign in with an existing account from user_data.
        </small>
      </div>
    </div>
  );
}

function App() {
  const [resources, setResources] = useState([]);
  const [resourcesLoading, setResourcesLoading] = useState(true);
  const [expandedMenus, setExpandedMenus] = useState({
    salaries: true,
    transactions: true,
    "credit-card-transactions": true,
    loans: true,
  });
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("fna_user") || "null");
    } catch {
      return null;
    }
  });
  const location = useLocation();
  const authenticated = !!localStorage.getItem("fna_token");
  useEffect(() => {
    if (!authenticated) {
      setResourcesLoading(false);
      return;
    }
    setResourcesLoading(true);
    api
      .get("/resources")
      .then((response) => setResources(response.data))
      .catch(() => {})
      .finally(() => setResourcesLoading(false));
  }, [authenticated, user]);
  if (authenticated && resourcesLoading)
    return (
      <div className="app-shell">
        <main className="main-area">
          <div className="page-content">Loading workspace...</div>
        </main>
      </div>
    );
  if (!authenticated)
    return location.pathname === "/login" ? (
      <Login onLogin={setUser} />
    ) : (
      <Navigate to="/login" replace />
    );
  if (location.pathname === "/login") return <Navigate to="/" replace />;
  const logout = () => {
    localStorage.removeItem("fna_token");
    localStorage.removeItem("fna_user");
    setUser(null);
    setResources([]);
  };
  const pageLabels = {
    "/debtor-balance": "Debtor Balance",
    "/search-transactions": "Search Transaction",
    "/compute-balance": "Search Transaction",
    "/transaction-details": "Search Transaction",
    "/card-transaction-details": "Search Card Transactions",
    "/compute-card-balance": "Compute Card Balance",
    "/search-loan": "Search Loan",
    "/loan-details": "Search Loan",
    "/search-salary": "Search Salary",
    "/salary-details": "Search Salary",
  };
  const currentLabel =
    resources.find((resource) => `/${resource.key}` === location.pathname)
      ?.label ||
    pageLabels[location.pathname] ||
    "Overview";
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">
            <i className="bi bi-wallet2" />
          </div>
          <div>
            <b>PayBook</b>
            <small>Personal Finance</small>
          </div>
        </div>
        <div className="nav-caption">WORKSPACE</div>
        <NavLink
          end
          to="/"
          className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
        >
          <i className="bi bi-grid-1x2" />
          Dashboard
        </NavLink>
        <div className="nav-caption mt-4">Functionalities</div>
        {resources.map((resource) => (
          <React.Fragment key={resource.key}>
            {menuOnlySections.includes(resource.key) ? (
              <button
                type="button"
                className="nav-item nav-menu-toggle"
                aria-expanded={expandedMenus[resource.key]}
                onClick={() =>
                  setExpandedMenus((previous) => ({
                    ...previous,
                    [resource.key]: !previous[resource.key],
                  }))
                }
              >
                <i className={`bi ${icons[resource.key] || "bi-table"}`} />
                <span>{resource.label}</span>
                <i className={`bi bi-chevron-down nav-menu-chevron ${expandedMenus[resource.key] ? "expanded" : ""}`} aria-hidden="true" />
              </button>
            ) : (
              <NavLink
                to={`/${resource.key}`}
                className={({ isActive }) =>
                  `nav-item ${isActive ? "active" : ""}`
                }
              >
                <i className={`bi ${icons[resource.key] || "bi-table"}`} />
                {resource.label}
              </NavLink>
            )}
            {resource.key === "loans" && expandedMenus.loans && (
              <NavLink
                to="/search-loan"
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-card-list" />
                Search Loan
              </NavLink>
            )}
            {resource.key === "transactions" && expandedMenus.transactions && (
              <NavLink
                to="/search-transactions"
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-list-ul" />
                Search Transaction
              </NavLink>
            )}
            {resource.key === "transactions" && expandedMenus.transactions && (
              <NavLink
                to="/debtor-balance"
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-person-lines-fill" />
                Debtor Balance
              </NavLink>
            )}
            {resource.key === "credit-card-transactions" && expandedMenus[resource.key] && (
              <NavLink
                to="/card-transaction-details"
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-list-ul" />
                Search Card Transactions
              </NavLink>
            )}
            {resource.key === "credit-card-transactions" && expandedMenus[resource.key] && (
              <NavLink
                to="/compute-card-balance"
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-calculator" />
                Compute Card Balance
              </NavLink>
            )}
            {resource.key === "salaries" && expandedMenus.salaries && (
              <NavLink
                to="/search-salary"
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-card-list" />
                Search Salary
              </NavLink>
            )}
            {menuOnlySections.includes(resource.key) && expandedMenus[resource.key] && (
              <NavLink
                to={`/${resource.key}`}
                className={({ isActive }) =>
                  `nav-item subnav-item ${isActive ? "active" : ""}`
                }
              >
                <i className="bi bi-pencil-square" />
                Add / Modify Records
              </NavLink>
            )}
          </React.Fragment>
        ))}
        <div className="sidebar-bottom">
          <span className="status-dot" /> Personal workspace
          <small>Signed in as {user?.first_name || "User"}</small>
        </div>
      </aside>
      <main className="main-area">
        <header className="topbar">
          <div>
            <span className="text-muted small">My Finance / </span>
            <b>{currentLabel}</b>
          </div>
          <div className="profile">
            <div className="avatar">{user?.first_name?.[0] || "U"}</div>
            <div>
              <b>
                {user?.first_name} {user?.last_name || ""}
              </b>
              <small>{user?.email_id}</small>
            </div>
            <button
              className="btn btn-sm btn-outline-secondary ms-3"
              onClick={logout}
            >
              <i className="bi bi-box-arrow-right me-1" />
              Sign out
            </button>
          </div>
        </header>
        <div className="page-content">
          <Routes>
            <Route path="/" element={<Dashboard user={user} />} />
            <Route path="/debtor-balance" element={<DebtorBalancePage />} />
            <Route
              path="/search-transactions"
              element={<TransactionDetailsPage />}
            />
            <Route path="/search-loan" element={<LoanDetailsPage />} />
            <Route
              path="/loan-details"
              element={<Navigate to="/search-loan" replace />}
            />
            <Route path="/search-salary" element={<SalaryDetailsPage />} />
            <Route
              path="/salary-details"
              element={<Navigate to="/search-salary" replace />}
            />
            <Route
              path="/compute-balance"
              element={<Navigate to="/search-transactions" replace />}
            />
            <Route
              path="/transaction-details"
              element={<Navigate to="/search-transactions" replace />}
            />
            <Route
              path="/card-transaction-details"
              element={<CardTransactionDetailsPage />}
            />
            <Route
              path="/compute-card-balance"
              element={<CardTransactionDetailsPage pageTitle="Compute Card Balance" />}
            />
            {resources.map((resource) => (
              <Route
                key={resource.key}
                path={`/${resource.key}`}
                element={<ResourcePage resource={resource} />}
              />
            ))}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

function Dashboard({ user }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [transactionFilter, setTransactionFilter] = useState("ALL");
  useEffect(() => {
    api
      .get("/dashboard", { params: { transaction_type: transactionFilter } })
      .then((response) => setData(response.data))
      .catch(() => setError("Unable to load dashboard."));
  }, [transactionFilter]);

  const salary = Number(data?.total_salary || 0);
  const loanPrincipal = Number(data?.total_loan_principal || 0);
  const transactionTotal = Number(data?.total_transaction_amount || 0);
  const chartData = (data?.recent_transactions || []).slice(0, 7).reverse().map((row, index) => ({
    name: `#${index + 1}`,
    credit: row.transaction_type === "C" ? Number(row.amount || 0) : 0,
    debit: row.transaction_type === "D" ? Number(row.amount || 0) : 0,
  }));

  return (
    <div className="dashboard-page">
      <div className="dashboard-heading">
        <div>
          <div className="eyebrow">PERSONAL OVERVIEW</div>
          <h1>Dashboard</h1>
          <p>Welcome back, {user?.first_name || "there"}</p>
        </div>
        <div className="dashboard-heading-actions">
          <button className="icon-button" title="Notifications"><i className="bi bi-bell" /></button>
          <span className="private-chip"><i className="bi bi-shield-check" /> Private workspace</span>
        </div>
      </div>
      {error && <div className="alert alert-warning">{error}</div>}

      <div className="dashboard-layout">
        <div className="dashboard-main-column">
          <div className="balance-grid">
            <div className="overview-card balance-card">
              <div className="overview-card-head"><span>Total Salary</span><i className="bi bi-cash-stack" /></div>
              <b>{data ? money(salary) : "—"}</b>
              <div className="balance-breakdown">
                <div><small><i className="bi bi-arrow-up-right" /> Salary records</small><strong>{data?.salaries ?? 0}</strong></div>
                <div><small><i className="bi bi-arrow-down-right" /> Accounts</small><strong>{data?.accounts ?? 0}</strong></div>
              </div>
            </div>
            <div className="overview-card balance-card savings-card">
              <div className="overview-card-head"><span>Transaction Value</span><i className="bi bi-arrow-left-right" /></div>
              <b>{data ? money(transactionTotal) : "—"}</b>
              <div className="mini-bars" aria-hidden="true">
                {[32, 44, 38, 58, 52, 66, 74].map((height, index) => <span key={index} style={{ height: `${height}%` }} />)}
              </div>
            </div>
          </div>

          <div className="overview-card statistics-card">
            <div className="statistics-head">
              <div><span>Statistics</span><small>Recent transaction activity</small></div>
              <div className="chart-legend"><span className="expense-dot">Debit Transactions</span><span className="income-dot">Credit Transactions</span></div>
            </div>
            <div className="dashboard-chart">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} barGap={5}>
                  <CartesianGrid stroke="#e6eaf2" vertical={false} />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: "#929caf", fontSize: 11 }} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fill: "#929caf", fontSize: 10 }} tickFormatter={(value) => value >= 1000 ? `${value / 1000}k` : value} />
                  <Tooltip formatter={(value) => money(value)} cursor={{ fill: "#f5f7fb" }} />
                  <Bar dataKey="debit" fill="#4d3c66" radius={[4, 4, 0, 0]} maxBarSize={18} />
                  <Bar dataKey="credit" fill="#8cf2dd" radius={[4, 4, 0, 0]} maxBarSize={18} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="dashboard-bottom-grid">
            <div className="overview-card insight-card loan-insight">
              <span>Loan Portfolio</span>
              <small>Outstanding principal recorded</small>
              <div className="insight-value"><i className="bi bi-bank2" /><b>{data ? money(loanPrincipal) : "—"}</b></div>
              <div className="progress-track"><span style={{ width: "62%" }} /></div>
            </div>
            <div className="overview-card insight-card account-insight">
              <span>Account Overview</span>
              <small>Bank accounts and records</small>
              <div className="account-pills">
                <div><b>{data?.accounts ?? 0}</b><small>Accounts</small></div>
                <div><b>{data?.transactions ?? 0}</b><small>Transactions</small></div>
              </div>
            </div>
          </div>
        </div>

        <aside className="transactions-panel">
          <div className="transactions-panel-head">
            <div><h2>Transactions</h2><small>Recent activity</small></div>
            <NavLink to="/transactions" className="view-all">View all</NavLink>
          </div>
          <div className="transaction-filter" role="group" aria-label="Filter recent transactions">
            {[
              ["ALL", "All"],
              ["D", "Debit Transactions"],
              ["C", "Credit Transactions"],
            ].map(([value, label]) => (
              <button
                type="button"
                key={value}
                className={transactionFilter === value ? "active" : ""}
                onClick={() => setTransactionFilter(value)}
              >
                {label}
              </button>
            ))}
          </div>
          <div className="transaction-list">
            {data?.recent_transactions?.length ? data.recent_transactions.map((row) => {
              const credit = row.transaction_type === "C";
              return (
                <div className="transaction-row" key={row.transaction_id}>
                  <div className={`transaction-icon ${credit ? "credit" : "debit"}`}><i className={`bi ${credit ? "bi-arrow-down-left" : "bi-arrow-up-right"}`} /></div>
                  <div className="transaction-name"><b>{row.counterparty_name || row.remarks || "Transaction"}</b><small>{row.created_date ? new Date(row.created_date).toLocaleDateString() : "Recent"}</small></div>
                  <strong className={credit ? "credit" : "debit"}>{credit ? "+" : "-"}{money(row.amount)}</strong>
                </div>
              );
            }) : <div className="transaction-empty">{data ? "No transactions found." : "Loading transactions..."}</div>}
          </div>
        </aside>
      </div>
    </div>
  );
}

function ResourcePage({ resource }) {
  const pageSize = 25;
  const [items, setItems] = useState([]),
    [search, setSearch] = useState(""),
    [page, setPage] = useState(1),
    [total, setTotal] = useState(0),
    [loading, setLoading] = useState(true),
    [editing, setEditing] = useState(null),
    [show, setShow] = useState(false),
    [error, setError] = useState("");
  const load = () => {
    setLoading(true);
    api
      .get(`/${resource.key}`, {
        params: { q: search, limit: pageSize, offset: (page - 1) * pageSize },
      })
      .then((response) => {
        setTotal(response.data.total);
        if (
          !response.data.items.length &&
          page > 1 &&
          response.data.total <= (page - 1) * pageSize
        ) {
          setPage(Math.max(1, Math.ceil(response.data.total / pageSize)));
          return;
        }
        setItems(response.data.items);
      })
      .catch((requestError) =>
        setError(
          requestError.response?.data?.detail || "Unable to load records",
        ),
      )
      .finally(() => setLoading(false));
  };
  useEffect(() => {
    load();
  }, [resource.key, search, page]);
  const fields = resource.fields.filter(
    (field) =>
      !["created_by", "modified_by"].includes(field.name) &&
      (!field.read_only ||
        (field.name === "created_date" && dateResources.includes(resource.key))),
  );
  const displayFields = resource.fields
    .filter((field) => !["created_by", "modified_by", "login_password"].includes(field.name))
    .slice(0, 7);
  const save = async (form) => {
    try {
      if (editing)
        await api.put(
          `/${resource.key}/${editing[resource.primary_key]}`,
          form,
        );
      else await api.post(`/${resource.key}`, form);
      setShow(false);
      setEditing(null);
      load();
    } catch (requestError) {
      setError(
        typeof requestError.response?.data?.detail === "string"
          ? requestError.response.data.detail
          : "Please check the entered values and references.",
      );
    }
  };
  const remove = async (row) => {
    if (!confirm("Delete this record?")) return;
    try {
      await api.delete(`/${resource.key}/${row[resource.primary_key]}`);
      load();
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail || "Unable to delete record",
      );
    }
  };
  const statusClass = (row) =>
    statusResources.includes(resource.key)
      ? { A: "status-active", E: "status-expired" }[row.status_code] || ""
      : "";
  const valueFor = (field, row) => {
    if (field.name === "status_code" && statusResources.includes(resource.key))
      return (
        <span className={`record-status ${statusClass(row)}`}>
          {row.status_code === "A"
            ? "Active"
            : row.status_code === "E"
              ? "Expired"
              : row.status_code}
        </span>
      );
    if (resource.key === "accounts" && field.name === "account_type")
      return accountTypeLabel(row[field.name]) || "—";
    if (field.name === "transaction_type")
      return row[field.name] === "C"
        ? "Credit"
        : row[field.name] === "D"
          ? "Debit"
          : (row[field.name] ?? "—");
    if (
      ["amount", "principal", "emi", "interest", "loan_amount"].includes(
        field.name,
      )
    )
      return money(row[field.name]);
    if (field.name === "counterparty_name") return row[field.name] || "—";
    return String(row[field.name] ?? "—");
  };
  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">
            MY FINANCES / {resource.label.toUpperCase()}
          </div>
          <h1>{resource.label}</h1>
          <p>Only records belonging to your login are displayed.</p>
        </div>
        <button
          className="btn btn-primary px-3"
          onClick={() => {
            setEditing(null);
            setShow(true);
          }}
        >
          <i className="bi bi-plus-lg me-2" />
          Add record
        </button>
      </div>
      {error && (
        <div className="alert alert-warning">
          {error}
          <button
            className="btn-close float-end"
            onClick={() => setError("")}
          />
        </div>
      )}
      <div className="table-card">
        <div className="table-toolbar">
          <div>
            <b>My records</b>
            <small>
              {total === 0 ? 0 : (page - 1) * pageSize + 1}–
              {Math.min(page * pageSize, total)} of {total}
            </small>
          </div>
          <div className="search-box">
            <i className="bi bi-search" />
            <input
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                setPage(1);
              }}
              placeholder="Search records..."
            />
          </div>
        </div>
        <div className="table-responsive">
          <table className="table align-middle mb-0">
            <thead>
              <tr>
                {displayFields.map((field) => (
                    <th key={field.name}>{title(field.name)}</th>
                ))}
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-5 text-muted">
                    Loading...
                  </td>
                </tr>
              ) : items.length ? (
                items.map((row) => (
                  <tr
                    key={row[resource.primary_key]}
                    className={statusClass(row)}
                  >
                    {displayFields.map((field) => (
                        <td key={field.name}>{valueFor(field, row)}</td>
                    ))}
                    <td className="text-end text-nowrap">
                      <button
                        className="action-btn"
                        title={
                          resource.key === "transactions" && Number(row.account_id) === 1
                            ? "Cash account transactions cannot be edited"
                            : "Edit"
                        }
                        disabled={
                          resource.key === "transactions" && Number(row.account_id) === 1
                        }
                        onClick={() => {
                          setEditing(row);
                          setShow(true);
                        }}
                      >
                        <i className="bi bi-pencil" />
                      </button>
                      <button
                        className="action-btn danger"
                        title="Delete"
                        onClick={() => remove(row)}
                      >
                        <i className="bi bi-trash3" />
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="8" className="text-center py-5 text-muted">
                    No records found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <div className="d-flex justify-content-between align-items-center px-3 py-2 border-top">
          <button
            className="btn btn-sm btn-outline-secondary"
            disabled={loading || page === 1}
            onClick={() => setPage(page - 1)}
          >
            <i className="bi bi-chevron-left me-1" />
            Previous
          </button>
          <small className="text-muted">
            Page {page} of {Math.max(1, Math.ceil(total / pageSize))}
          </small>
          <button
            className="btn btn-sm btn-outline-secondary"
            disabled={loading || page * pageSize >= total}
            onClick={() => setPage(page + 1)}
          >
            Next
            <i className="bi bi-chevron-right ms-1" />
          </button>
        </div>
      </div>
      {show && (
        <RecordModal
          key={`${resource.key}-${editing?.[resource.primary_key] ?? "new"}`}
          resource={resource}
          fields={fields}
          initial={editing || {}}
          onClose={() => setShow(false)}
          onSave={save}
        />
      )}
    </>
  );
}

function RecordModal({ resource, fields, initial, onClose, onSave }) {
  const [people, setPeople] = useState([]),
    [accounts, setAccounts] = useState([]),
    [companies, setCompanies] = useState([]),
    [designations, setDesignations] = useState([]);
  const [loanPaymentMode, setLoanPaymentMode] = useState(() =>
    resource.key !== "loans"
      ? "emi"
      : !initial.payment_id ||
          (Number(initial.emi || 0) > 0 &&
            Math.abs(
              Number(initial.principal || 0) -
                (Number(initial.emi || 0) - Number(initial.interest || 0)),
            ) < 0.005)
        ? "emi"
        : "part-payment",
  );
  const [formError, setFormError] = useState("");
  const [form, setForm] = useState(() =>
    Object.fromEntries(
      fields.map((field) => {
        if (resource.key === "accounts" && field.name === "loan_amount")
          return [field.name, initial[field.name] ?? 0];
        if (
          field.name === "created_date" &&
          dateResources.includes(resource.key)
        )
          return [field.name, initial[field.name]?.slice(0, 10) || todayDate()];
        if (resource.key === "loans" && field.name === "interest")
          return [field.name, initial[field.name] ?? 0];
        if (field.name === "transaction_type")
          return [
            field.name,
            String(initial[field.name] || "C")
              .toUpperCase()
              .startsWith("D")
              ? "D"
              : "C",
          ];
        if (
          field.name === "status_code" &&
          statusResources.includes(resource.key)
        )
          return [field.name, initial[field.name] ?? "E"];
        return [field.name, initial[field.name] ?? ""];
      }),
    ),
  );
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    if (resource.key === "transactions")
      api
        .get("/people")
        .then((response) => setPeople(response.data))
        .catch(() => {});
    if (["salaries", "loans", "transactions"].includes(resource.key))
      api
        .get("/accounts", { params: { limit: 500 } })
        .then((response) => {
          const rows = response.data.items;
          setAccounts(
            resource.key === "salaries"
              ? rows.filter(
                  (account) =>
                    String(account.account_type || "")
                      .trim()
                      .toUpperCase() !== "L",
                )
              : rows,
          );
        })
        .catch(() => {});
    if (resource.key === "salaries")
      Promise.all([
        api.get("/companies", { params: { limit: 500 } }),
        api.get("/designations", { params: { limit: 500 } }),
      ])
        .then(([companiesResponse, designationsResponse]) => {
          setCompanies(
            companiesResponse.data.items.filter(
              (row) => !row.status_code || row.status_code === "A",
            ),
          );
          setDesignations(
            designationsResponse.data.items.filter(
              (row) => !row.status_code || row.status_code === "A",
            ),
          );
        })
        .catch(() => {});
  }, [resource.key]);
  const change = (name, value) =>
    setForm((previous) => ({
      ...previous,
      [name]: value,
      ...(resource.key === "accounts" &&
      name === "account_type" &&
      value !== "L"
        ? { loan_amount: 0 }
        : {}),
    }));
  const changeLoanPaymentMode = (mode) => {
    setLoanPaymentMode(mode);
    setFormError("");
    setForm((previous) =>
      mode === "part-payment"
        ? { ...previous, emi: 0, interest: 0 }
        : { ...previous, emi: "", interest: 0 },
    );
  };
  const submit = async (event) => {
    event.preventDefault();
    setFormError("");
    setBusy(true);
    const data = { ...form };
    fields.forEach((field) => {
      if (field.name === "transaction_type")
        data[field.name] = String(data[field.name] || "C")
          .charAt(0)
          .toUpperCase();
      else if (data[field.name] === "") data[field.name] = null;
      else if (/numeric|integer|bigint/i.test(field.type))
        data[field.name] = Number(data[field.name]);
    });
    if (resource.key === "loans") {
      if (loanPaymentMode === "emi") {
        const principal = Number(data.emi || 0) - Number(data.interest || 0);
        if (principal <= 0) {
          setFormError(
            "EMI must be greater than Interest so Principal is positive.",
          );
          setBusy(false);
          return;
        }
        data.principal = principal;
      } else {
        data.emi = 0;
        data.interest = 0;
        data.principal = Number(data.principal || 0);
      }
    }
    if (resource.key === "accounts" && data.account_type !== "L")
      data.loan_amount = 0;
    if (
      resource.key === "accounts" &&
      data.account_type === "L" &&
      data.loan_amount == null
    )
      data.loan_amount = 0;
    await onSave(data);
    setBusy(false);
  };
  const renderField = (field) => {
    const value = form[field.name] ?? "";
    const positive = ["amount", "principal", "emi", "loan_amount"].includes(
      field.name,
    );
    const required =
      field.name === "loan_amount" && resource.key === "accounts"
        ? form.account_type === "L"
        : field.required;
    if (
      resource.key === "loans" &&
      field.name === "principal" &&
      loanPaymentMode === "emi"
    )
      return null;
    if (
      resource.key === "loans" &&
      ["emi", "interest"].includes(field.name) &&
      loanPaymentMode === "part-payment"
    )
      return (
        <input className="form-control" type="number" value="0" disabled />
      );
    if (field.name === "created_date" && dateResources.includes(resource.key))
      return (
        <input
          className="form-control"
          type="date"
          value={value}
          required
          onChange={(event) => change(field.name, event.target.value)}
        />
      );
    if (field.name === "debtor_id")
      return (
        <select
          className="form-select"
          value={value}
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="">Select Debtor</option>
          {people.map((person) => (
            <option key={person.user_id} value={person.user_id}>
              {[person.first_name, person.middle_name, person.last_name]
                .filter(Boolean)
                .join(" ")}
              {person.email_id ? ` (${person.email_id})` : ""}
            </option>
          ))}
        </select>
      );
    if (resource.key === "accounts" && field.name === "account_type")
      return (
        <select
          className="form-select"
          value={value}
          required
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="">Select account type</option>
          <option value="S">Saving</option>
          <option value="C">Current</option>
          <option value="L">Loan</option>
        </select>
      );
    if (
      ["salaries", "loans", "transactions"].includes(resource.key) &&
      field.name === "account_id"
    )
      return (
        <select
          className="form-select"
          value={value}
          required={field.required}
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="">Select Account</option>
          {accounts.map((account) => (
            <option key={account.account_id} value={account.account_id}>
              {[
                account.bank_name,
                account.account_no,
                accountTypeLabel(account.account_type),
              ]
                .filter(Boolean)
                .join(" · ")}
            </option>
          ))}
        </select>
      );
    if (resource.key === "salaries" && field.name === "company_id")
      return (
        <select
          className="form-select"
          value={value}
          required={field.required}
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="">Select Company</option>
          {companies.map((company) => (
            <option key={company.company_id} value={company.company_id}>
              {company.company_name}
            </option>
          ))}
        </select>
      );
    if (resource.key === "salaries" && field.name === "designation_id")
      return (
        <select
          className="form-select"
          value={value}
          required={field.required}
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="">Select Designation</option>
          {designations.map((designation) => (
            <option
              key={designation.designation_id}
              value={designation.designation_id}
            >
              {designation.designation_name}
            </option>
          ))}
        </select>
      );
    if (field.name === "status_code" && statusResources.includes(resource.key))
      return (
        <select
          className="form-select"
          value={value || "E"}
          required
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="A">Active</option>
          <option value="E">Expired</option>
        </select>
      );
    if (
      field.name === "transaction_type" &&
      transactionResources.includes(resource.key)
    )
      return (
        <select
          className="form-select"
          value={value || "C"}
          required={field.required}
          onChange={(event) => change(field.name, event.target.value)}
        >
          <option value="C">Credit</option>
          <option value="D">Debit</option>
        </select>
      );
    if (field.type.toLowerCase().includes("date"))
      return (
        <input
          className="form-control"
          type="date"
          value={value}
          required={field.required}
          onChange={(event) => change(field.name, event.target.value)}
        />
      );
    if (field.type.toLowerCase().includes("text"))
      return (
        <textarea
          className="form-control"
          rows="2"
          value={value}
          onChange={(event) => change(field.name, event.target.value)}
        />
      );
    return (
      <input
        className="form-control"
        type={/numeric|integer|bigint/i.test(field.type) ? "number" : "text"}
        min={
          positive
            ? "0.01"
            : field.name === "interest" && resource.key === "loans"
              ? "0"
              : undefined
        }
        step={
          positive || (field.name === "interest" && resource.key === "loans")
            ? "0.01"
            : undefined
        }
        value={value}
        required={required}
        onChange={(event) => change(field.name, event.target.value)}
      />
    );
  };
  return (
    <div
      className="modal-backdrop-custom"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div className="record-modal">
        <div className="modal-head">
          <div>
            <div className="eyebrow">
              {initial[resource.primary_key] ? "UPDATE RECORD" : "NEW RECORD"}
            </div>
            <h3>
              {initial[resource.primary_key] ? "Edit" : "Add"} {resource.label}
            </h3>
          </div>
          <button className="btn-close" onClick={onClose} />
        </div>
        <form onSubmit={submit}>
          {resource.key === "loans" && (
            <fieldset className="mb-3">
              <legend className="form-label fw-semibold">
                Are you paying as EMI or Part Payment?
              </legend>
              <div className="d-flex gap-4">
                <label className="form-check">
                  <input
                    className="form-check-input"
                    type="radio"
                    name="loan-payment-mode"
                    checked={loanPaymentMode === "emi"}
                    onChange={() => changeLoanPaymentMode("emi")}
                  />
                  <span className="form-check-label">EMI</span>
                </label>
                <label className="form-check">
                  <input
                    className="form-check-input"
                    type="radio"
                    name="loan-payment-mode"
                    checked={loanPaymentMode === "part-payment"}
                    onChange={() => changeLoanPaymentMode("part-payment")}
                  />
                  <span className="form-check-label">Part Payment</span>
                </label>
              </div>
            </fieldset>
          )}
          {formError && (
            <div className="alert alert-danger py-2">{formError}</div>
          )}
          <div className="form-grid">
            {fields.map((field) =>
              resource.key === "accounts" &&
              field.name === "loan_amount" &&
              form.account_type !== "L" ? null : resource.key === "loans" &&
                field.name === "principal" &&
                loanPaymentMode === "emi"
                ? null
                : (
                <div className="form-field" key={field.name}>
                  <label>
                    {labelForField(field, resource)}{" "}
                    {(field.required ||
                      (resource.key === "accounts" &&
                        field.name === "loan_amount" &&
                        form.account_type === "L")) && (
                      <span className="text-danger">*</span>
                    )}
                  </label>
                  {renderField(field)}
                </div>
              ),
            )}
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-light" onClick={onClose}>
              Cancel
            </button>
            <button disabled={busy} className="btn btn-primary px-4">
              {busy ? "Saving..." : "Save record"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function labelForField(field, resource) {
  if (field.name === "account_id" && resource.key === "salaries")
    return "Account";
  if (field.name === "company_id" && resource.key === "salaries")
    return "Company";
  if (field.name === "designation_id" && resource.key === "salaries")
    return "Designation";
  if (field.name === "debtor_id") return "Debtors";
  if (field.name === "status_code") return "Status";
  if (field.name === "transaction_type") return "Transaction Type";
  return title(field.name);
}

function DebtorBalancePage() {
  const [people, setPeople] = useState([]),
    [debtorId, setDebtorId] = useState(""),
    [balance, setBalance] = useState(null),
    [loadingPeople, setLoadingPeople] = useState(true),
    [loadingBalance, setLoadingBalance] = useState(false),
    [error, setError] = useState("");
  useEffect(() => {
    api
      .get("/people")
      .then((response) => setPeople(response.data))
      .catch(() => setError("Unable to load debtors."))
      .finally(() => setLoadingPeople(false));
  }, []);
  const selectDebtor = async (event) => {
    const id = event.target.value;
    setDebtorId(id);
    setBalance(null);
    setError("");
    if (!id) return;
    setLoadingBalance(true);
    try {
      const response = await api.get(`/debtors/${id}/balance`);
      setBalance(response.data);
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail || "Unable to load debtor balance.",
      );
    } finally {
      setLoadingBalance(false);
    }
  };
  const amount = Number(balance?.balance || 0),
    color = loadingBalance
      ? ""
      : amount === 0
        ? "balance-zero"
        : amount > 0
          ? "balance-positive"
          : "balance-negative";
  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">PEOPLE / BALANCES</div>
          <h1>Debtor Balance</h1>
          <p>Review credit and debit totals for a selected debtor.</p>
        </div>
      </div>
      <div className="table-card p-3 mb-4">
        <label className="form-label fw-semibold" htmlFor="debtor-balance-user">
          Debtors
        </label>
        <select
          id="debtor-balance-user"
          className="form-select"
          value={debtorId}
          disabled={loadingPeople}
          onChange={selectDebtor}
        >
          <option value="">
            {loadingPeople ? "Loading debtors..." : "Select Debtor"}
          </option>
          {people.map((person) => (
            <option key={person.user_id} value={person.user_id}>
              {[person.first_name, person.middle_name, person.last_name]
                .filter(Boolean)
                .join(" ")}
              {person.email_id ? ` (${person.email_id})` : ""}
            </option>
          ))}
        </select>
      </div>
      {error && <div className="alert alert-warning">{error}</div>}
      {debtorId && (
        <div className="summary-grid">
          <div className="summary-card">
            <span>Credit Total</span>
            <b>
              {loadingBalance ? "Loading..." : money(balance?.credit_total)}
            </b>
          </div>
          <div className="summary-card">
            <span>Debit Total</span>
            <b>{loadingBalance ? "Loading..." : money(balance?.debit_total)}</b>
          </div>
          <div className="summary-card">
            <span>Balance (Debit − Credit)</span>
            <b className={color}>
              {loadingBalance ? "Loading..." : money(balance?.balance)}
            </b>
          </div>
        </div>
      )}
    </>
  );
}

function TransactionDetailsPage() {
  const [people, setPeople] = useState([]),
    [accounts, setAccounts] = useState([]),
    [debtorId, setDebtorId] = useState(""),
    [accountId, setAccountId] = useState("0"),
    [type, setType] = useState("ALL"),
    [fromDate, setFromDate] = useState(""),
    [toDate, setToDate] = useState(todayDate()),
    [rows, setRows] = useState([]),
    [loading, setLoading] = useState(false),
    [error, setError] = useState("");
  useEffect(() => {
    Promise.all([
      api.get("/people"),
      api.get("/accounts", { params: { limit: 500 } }),
    ])
      .then(([p, a]) => {
        setPeople(p.data);
        setAccounts(a.data.items);
      })
      .catch(() => setError("Unable to load filters."));
  }, []);
  const search = async (event) => {
    event.preventDefault();
    setError("");
    setRows([]);
    if (!debtorId || !accountId || !fromDate || !toDate) {
      setError("Select a Debtor, Account, From Date, and To Date.");
      return;
    }
    if (fromDate > toDate) {
      setError("From Date must be on or before To Date.");
      return;
    }
    setLoading(true);
    try {
      const response = await api.get("/transaction-details", {
        params: {
          account_id: accountId,
          debtor_id: debtorId,
          transaction_type: type,
          from_date: fromDate,
          to_date: toDate,
        },
      });
      setRows(response.data);
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail || "Unable to load transactions.",
      );
    } finally {
      setLoading(false);
    }
  };
  const columns = [
    { key: "transaction_id", label: "Transaction ID", value: (row) => row.transaction_id },
    { key: "debtor_name", label: "Debtor" },
    { key: "remarks", label: "Remarks" },
    { key: "transaction_type", label: "Type", value: (row) => row.transaction_type === "C" ? "Credit" : row.transaction_type === "D" ? "Debit" : row.transaction_type },
    { key: "created_date", label: "Date", value: (row) => row.created_date ? new Date(row.created_date).toLocaleDateString() : "—" },
    { key: "account_no", label: "Account No." },
    { key: "credited_amount", label: "Credit", className: "text-end", value: (row) => plainAmount(row.credited_amount) },
    { key: "debited_amount", label: "Debit", className: "text-end", value: (row) => plainAmount(row.debited_amount) },
  ];
  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">PEOPLE / DEBTOR BALANCE</div>
          <h1>Search Transaction</h1>
          <p>Filter transactions by debtor, account, type, and date range.</p>
        </div>
      </div>
      <form className="table-card p-3 mb-4" onSubmit={search}>
        <div className="form-grid">
          <div className="form-field">
            <label htmlFor="details-debtor">Debtors *</label>
            <select
              id="details-debtor"
              className="form-select"
              value={debtorId}
              required
              onChange={(event) => setDebtorId(event.target.value)}
            >
              <option value="">Select Debtor</option>
              {people.map((person) => (
                <option key={person.user_id} value={person.user_id}>
                  {[person.first_name, person.middle_name, person.last_name]
                    .filter(Boolean)
                    .join(" ")}
                  {person.email_id ? ` (${person.email_id})` : ""}
                </option>
              ))}
            </select>
          </div>
          <div className="form-field">
            <label htmlFor="details-account">Account *</label>
            <select
              id="details-account"
              className="form-select"
              value={accountId}
              required
              onChange={(event) => setAccountId(event.target.value)}
            >
              <option value="0">Select Account</option>
              {accounts.map((account) => (
                <option key={account.account_id} value={account.account_id}>
                  {[
                    account.bank_name,
                    account.account_no,
                    accountTypeLabel(account.account_type),
                  ]
                    .filter(Boolean)
                    .join(" · ")}
                </option>
              ))}
            </select>
          </div>
          <div className="form-field">
            <label htmlFor="details-type">Transaction Type *</label>
            <select
              id="details-type"
              className="form-select"
              value={type}
              onChange={(event) => setType(event.target.value)}
            >
              <option value="ALL">ALL</option>
              <option value="D">Debit</option>
              <option value="C">Credit</option>
            </select>
          </div>
          <div className="form-field">
            <label htmlFor="details-from">From Date *</label>
            <input
              id="details-from"
              className="form-control"
              type="date"
              value={fromDate}
              required
              onChange={(event) => setFromDate(event.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="details-to">To Date *</label>
            <input
              id="details-to"
              className="form-control"
              type="date"
              value={toDate}
              required
              onChange={(event) => setToDate(event.target.value)}
            />
          </div>
        </div>
        <div className="d-flex justify-content-end mt-3">
          <button className="btn btn-primary px-3" disabled={loading}>
            <i className="bi bi-search me-2" />
            {loading ? "Searching..." : "Search"}
          </button>
        </div>
      </form>
      {error && <div className="alert alert-warning">{error}</div>}
      <PaginatedExportTable columns={columns} rows={rows} loading={loading} emptyMessage={debtorId || accountId ? "No transactions found for these filters." : "Select filters and search."} filename="search-transaction" title="Search Transaction" />
    </>
  );
}

function LoanDetailsPage() {
  const [accounts, setAccounts] = useState([]),
    [accountId, setAccountId] = useState(""),
    [fromDate, setFromDate] = useState(""),
    [toDate, setToDate] = useState(todayDate()),
    [rows, setRows] = useState([]),
    [loadingAccounts, setLoadingAccounts] = useState(true),
    [loading, setLoading] = useState(false),
    [error, setError] = useState("");
  useEffect(() => {
    api
      .get("/accounts", { params: { limit: 500 } })
      .then((response) =>
        setAccounts(
          response.data.items.filter(
            (account) =>
              String(account.account_type || "")
                .trim()
                .toUpperCase() === "L",
          ),
        ),
      )
      .catch(() => setError("Unable to load loan accounts."))
      .finally(() => setLoadingAccounts(false));
  }, []);
  const search = async (event) => {
    event.preventDefault();
    setError("");
    setRows([]);
    if (!accountId || !fromDate || !toDate) {
      setError("Select a Loan Account, From Date, and To Date.");
      return;
    }
    if (fromDate > toDate) {
      setError("From Date must be on or before To Date.");
      return;
    }
    setLoading(true);
    try {
      const response = await api.get("/loan-details", {
        params: { account_id: accountId, from_date: fromDate, to_date: toDate },
      });
      setRows(response.data);
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail || "Unable to load loan details.",
      );
    } finally {
      setLoading(false);
    }
  };
  const columns = [
    { key: "payment_id", label: "Payment ID", value: (row) => `#${row.payment_id}` },
    { key: "account_no", label: "Account No." },
    { key: "emi", label: "EMI", className: "text-end", value: (row) => money(row.emi) },
    { key: "principal", label: "Principal", className: "text-end", value: (row) => money(row.principal) },
    { key: "interest", label: "Interest", className: "text-end", value: (row) => money(row.interest) },
    { key: "loan_amount", label: "Loan Amount", className: "text-end", value: (row) => money(row.loan_amount) },
    { key: "created_date", label: "Date", value: (row) => row.created_date ? new Date(row.created_date).toLocaleDateString() : "—" },
  ];
  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">MY FINANCES / LOANS</div>
          <h1>Search Loan</h1>
          <p>
            Review loan payments for a selected loan account and date range.
          </p>
        </div>
      </div>
      <form className="table-card p-3 mb-4" onSubmit={search}>
        <div className="form-grid">
          <div className="form-field">
            <label htmlFor="loan-details-account">Loan Account *</label>
            <select
              id="loan-details-account"
              className="form-select"
              value={accountId}
              disabled={loadingAccounts}
              required
              onChange={(event) => setAccountId(event.target.value)}
            >
              <option value="">
                {loadingAccounts
                  ? "Loading loan accounts..."
                  : "Select Loan Account"}
              </option>
              {accounts.map((account) => (
                <option key={account.account_id} value={account.account_id}>
                  {[
                    account.bank_name,
                    account.account_no,
                    accountTypeLabel(account.account_type),
                  ]
                    .filter(Boolean)
                    .join(" · ")}
                </option>
              ))}
            </select>
          </div>
          <div className="form-field">
            <label htmlFor="loan-details-from">From Date *</label>
            <input
              id="loan-details-from"
              className="form-control"
              type="date"
              value={fromDate}
              required
              onChange={(event) => setFromDate(event.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="loan-details-to">To Date *</label>
            <input
              id="loan-details-to"
              className="form-control"
              type="date"
              value={toDate}
              required
              onChange={(event) => setToDate(event.target.value)}
            />
          </div>
        </div>
        <div className="d-flex justify-content-end mt-3">
          <button className="btn btn-primary px-3" disabled={loading}>
            <i className="bi bi-search me-2" />
            {loading ? "Searching..." : "Search"}
          </button>
        </div>
      </form>
      {error && <div className="alert alert-warning">{error}</div>}
      <PaginatedExportTable columns={columns} rows={rows} loading={loading} emptyMessage={accountId ? "No loan details found for these filters." : "Select a loan account and date range."} filename="search-loan" title="Search Loan" />
    </>
  );
}

function CardTransactionDetailsPage({ pageTitle = "Search Card Transactions" }) {
  const [bankCodes, setBankCodes] = useState([]),
    [bankCode, setBankCode] = useState(""),
    [transactionType, setTransactionType] = useState("ALL"),
    [fromDate, setFromDate] = useState(""),
    [toDate, setToDate] = useState(todayDate()),
    [rows, setRows] = useState([]),
    [loadingBankCodes, setLoadingBankCodes] = useState(true),
    [loading, setLoading] = useState(false),
    [error, setError] = useState("");

  useEffect(() => {
    api
      .get("/card-bank-codes")
      .then((response) => setBankCodes(response.data))
      .catch(() => setError("Unable to load bank codes."))
      .finally(() => setLoadingBankCodes(false));
  }, []);

  const search = async (event) => {
    event.preventDefault();
    setError("");
    setRows([]);
    if (!bankCode || !fromDate || !toDate) {
      setError("Select a Bank Code, From Date, and To Date.");
      return;
    }
    if (fromDate > toDate) {
      setError("From Date must be on or before To Date.");
      return;
    }
    setLoading(true);
    try {
      const response = await api.get("/card-transaction-details", {
        params: {
          bank_code: bankCode,
          tx_type: transactionType,
          from_date: fromDate,
          to_date: toDate,
        },
      });
      setRows(response.data);
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail ||
          "Unable to load card transaction details.",
      );
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { key: "transaction_id", label: "Transaction ID" },
    { key: "bank_code", label: "Bank Code" },
    { key: "remarks", label: "Description" },
    {
      key: "transaction_type",
      label: "Type",
      value: (row) => row.transaction_type === "C" ? "Credit" : "Debit",
    },
    {
      key: "created_date",
      label: "Date",
      value: (row) => row.created_date ? new Date(row.created_date).toLocaleDateString() : "—",
    },
    { key: "credited_amount", label: "Credit", className: "text-end", value: (row) => plainAmount(row.credited_amount) },
    { key: "debited_amount", label: "Debit", className: "text-end", value: (row) => plainAmount(row.debited_amount) },
  ];

  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">MY FINANCES / CARD TRANSACTIONS</div>
          <h1>{pageTitle}</h1>
          <p>Search card transactions by bank, type, and date range.</p>
        </div>
      </div>
      <form className="table-card p-3 mb-4" onSubmit={search}>
        <div className="form-grid">
          <div className="form-field">
            <label htmlFor="card-details-bank-code">Bank Code *</label>
            <select
              id="card-details-bank-code"
              className="form-select"
              value={bankCode}
              disabled={loadingBankCodes}
              required
              onChange={(event) => setBankCode(event.target.value)}
            >
              <option value="">
                {loadingBankCodes ? "Loading bank codes..." : "Select Bank Code"}
              </option>
              {bankCodes.map((code) => (
                <option key={code} value={code}>{code}</option>
              ))}
            </select>
          </div>
          {pageTitle !== "Compute Card Balance" && (
            <div className="form-field">
              <label htmlFor="card-details-transaction-type">Transaction Type *</label>
              <select
                id="card-details-transaction-type"
                className="form-select"
                value={transactionType}
                required
                onChange={(event) => setTransactionType(event.target.value)}
              >
                <option value="ALL">All</option>
                <option value="C">Credit</option>
                <option value="D">Debit</option>
              </select>
            </div>
          )}
          <div className="form-field">
            <label htmlFor="card-details-from">From Date *</label>
            <input
              id="card-details-from"
              className="form-control"
              type="date"
              value={fromDate}
              required
              onChange={(event) => setFromDate(event.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="card-details-to">To Date *</label>
            <input
              id="card-details-to"
              className="form-control"
              type="date"
              value={toDate}
              required
              onChange={(event) => setToDate(event.target.value)}
            />
          </div>
        </div>
        <div className="d-flex justify-content-end mt-3">
          <button className="btn btn-primary px-3" disabled={loading}>
            <i className="bi bi-search me-2" />
            {loading ? "Searching..." : "Search"}
          </button>
        </div>
      </form>
      {error && <div className="alert alert-warning">{error}</div>}
      <PaginatedExportTable
        columns={columns}
        rows={rows}
        loading={loading}
        emptyMessage={bankCode ? "No card transaction details found for these filters." : "Select a bank code and date range."}
        filename={pageTitle.toLowerCase().replaceAll(" ", "-")}
        title={pageTitle}
      />
    </>
  );
}

function SalaryDetailsPage() {
  const [companies, setCompanies] = useState([]),
    [companyId, setCompanyId] = useState(""),
    [fromDate, setFromDate] = useState(""),
    [toDate, setToDate] = useState(todayDate()),
    [rows, setRows] = useState([]),
    [loadingCompanies, setLoadingCompanies] = useState(true),
    [loading, setLoading] = useState(false),
    [error, setError] = useState("");
  useEffect(() => {
    api
      .get("/companies", { params: { limit: 500 } })
      .then((response) => setCompanies(response.data.items))
      .catch(() => setError("Unable to load companies."))
      .finally(() => setLoadingCompanies(false));
  }, []);
  const search = async (event) => {
    event.preventDefault();
    setError("");
    setRows([]);
    if (!companyId || !fromDate || !toDate) {
      setError("Select a Company, From Date, and To Date.");
      return;
    }
    if (fromDate > toDate) {
      setError("From Date must be on or before To Date.");
      return;
    }
    setLoading(true);
    try {
      const response = await api.get("/salary-details", {
        params: { company_id: companyId, from_date: fromDate, to_date: toDate },
      });
      setRows(response.data);
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail || "Unable to load salary details.",
      );
    } finally {
      setLoading(false);
    }
  };
  const columns = [
    { key: "sl_no", label: "Sl No" },
    { key: "salary_id", label: "Salary ID" },
    { key: "designation_name", label: "Designation" },
    { key: "salary_drawn", label: "Salary Drawn", value: (row) => money(row.salary_drawn) },
    { key: "account_no", label: "Account No." },
  ];
  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">MY FINANCES / SALARIES</div>
          <h1>Search Salary</h1>
          <p>Review salary records for a selected company and date range.</p>
        </div>
      </div>
      <form className="table-card p-3 mb-4" onSubmit={search}>
        <div className="form-grid">
          <div className="form-field">
            <label htmlFor="salary-details-company">Company *</label>
            <select
              id="salary-details-company"
              className="form-select"
              value={companyId}
              disabled={loadingCompanies}
              required
              onChange={(event) => setCompanyId(event.target.value)}
            >
              <option value="">
                {loadingCompanies ? "Loading companies..." : "Select Company"}
              </option>
              {companies.map((company) => (
                <option key={company.company_id} value={company.company_id}>
                  {company.company_name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-field">
            <label htmlFor="salary-details-from">From Date *</label>
            <input
              id="salary-details-from"
              className="form-control"
              type="date"
              value={fromDate}
              required
              onChange={(event) => setFromDate(event.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="salary-details-to">To Date *</label>
            <input
              id="salary-details-to"
              className="form-control"
              type="date"
              value={toDate}
              required
              onChange={(event) => setToDate(event.target.value)}
            />
          </div>
        </div>
        <div className="d-flex justify-content-end mt-3">
          <button className="btn btn-primary px-3" disabled={loading}>
            <i className="bi bi-search me-2" />
            {loading ? "Searching..." : "Search"}
          </button>
        </div>
      </form>
      {error && <div className="alert alert-warning">{error}</div>}
      <PaginatedExportTable columns={columns} rows={rows} loading={loading} emptyMessage={companyId ? "No salary details found for these filters." : "Select a company and date range."} filename="search-salary" title="Search Salary" />
    </>
  );
}

export default App;
