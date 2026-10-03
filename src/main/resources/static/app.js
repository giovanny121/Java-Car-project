const state = { customers: [], vehicles: [], rentals: [] };

// ---------- helpers ----------
async function api(path, options = {}) {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (res.status === 204) return null;
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    throw new Error((data && data.error) || `Request failed (${res.status})`);
  }
  return data;
}

function toast(message, isError = false) {
  const el = document.getElementById("toast");
  el.textContent = message;
  el.classList.toggle("error", isError);
  el.classList.add("show");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => el.classList.remove("show"), 2800);
}

function money(n) {
  return "$" + Number(n || 0).toFixed(2);
}

function esc(value) {
  return String(value ?? "").replace(/[&<>"']/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

// ---------- tabs ----------
document.getElementById("tabs").addEventListener("click", (e) => {
  const tab = e.target.closest(".tab");
  if (!tab) return;
  document.querySelectorAll(".tab").forEach((t) => t.classList.toggle("active", t === tab));
  document.querySelectorAll(".panel").forEach((p) =>
    p.classList.toggle("active", p.id === tab.dataset.tab));
  if (tab.dataset.tab === "dashboard") loadDashboard();
});

// ---------- data loading ----------
async function loadAll() {
  const [customers, vehicles, rentals] = await Promise.all([
    api("/api/customers"),
    api("/api/vehicles"),
    api("/api/rentals"),
  ]);
  state.customers = customers;
  state.vehicles = vehicles;
  state.rentals = rentals;
  renderCustomers();
  renderVehicles();
  renderRentals();
  renderRentalOptions();
}

async function loadDashboard() {
  const [summary, revenue, overdue] = await Promise.all([
    api("/api/reports/summary"),
    api("/api/reports/revenue-by-vehicle"),
    api("/api/reports/overdue"),
  ]);

  const cards = [
    { label: "Customers", value: summary.totalCustomers },
    { label: "Fleet", value: summary.totalVehicles },
    { label: "Available", value: summary.availableVehicles },
    { label: "Active rentals", value: summary.activeRentals },
    { label: "Overdue", value: summary.overdueRentals, cls: "overdue" },
    { label: "Total revenue", value: money(summary.totalRevenue), cls: "revenue" },
  ];
  document.getElementById("summaryCards").innerHTML = cards
    .map((c) => `<div class="stat ${c.cls || ""}"><div class="label">${c.label}</div><div class="value">${esc(c.value)}</div></div>`)
    .join("");

  document.getElementById("revenueTable").innerHTML = revenue.length
    ? revenue.map((r) => `<tr><td>${esc(r.licensePlate)}</td><td>${esc(r.vehicleType)}</td><td>${r.rentals}</td><td class="num">${money(r.revenue)}</td></tr>`).join("")
    : `<tr><td colspan="4" class="empty">No rentals yet</td></tr>`;

  document.getElementById("overdueTable").innerHTML = overdue.length
    ? overdue.map((r) => `<tr><td>${esc(r.customer.name)}</td><td>${esc(r.vehicle.licensePlate)}</td><td>${esc(r.dueDate)}</td></tr>`).join("")
    : `<tr><td colspan="3" class="empty">Nothing overdue</td></tr>`;
}

// ---------- customers ----------
function renderCustomers() {
  document.getElementById("customersTable").innerHTML = state.customers.length
    ? state.customers.map((c) => `
      <tr>
        <td>${esc(c.name)}</td>
        <td>${esc(c.age)}</td>
        <td>${esc(c.email)}</td>
        <td>${esc(c.phone)}</td>
        <td>${esc(c.licenseNumber)}</td>
        <td><div class="row-actions">
          <button class="icon-btn" data-edit-customer="${c.id}">Edit</button>
          <button class="icon-btn danger" data-del-customer="${c.id}">Delete</button>
        </div></td>
      </tr>`).join("")
    : `<tr><td colspan="6" class="empty">No customers yet</td></tr>`;
}

document.getElementById("customerForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const payload = {
    name: form.name.value.trim(),
    age: Number(form.age.value),
    email: form.email.value.trim(),
    phone: form.phone.value.trim(),
    licenseNumber: form.licenseNumber.value.trim(),
  };
  const id = form.id.value;
  try {
    await api(id ? `/api/customers/${id}` : "/api/customers", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload),
    });
    toast(id ? "Customer updated" : "Customer added");
    resetCustomerForm();
    await loadAll();
  } catch (err) { toast(err.message, true); }
});

document.getElementById("customerReset").addEventListener("click", resetCustomerForm);

function resetCustomerForm() {
  const form = document.getElementById("customerForm");
  form.reset();
  form.id.value = "";
  document.getElementById("customerFormTitle").textContent = "Add customer";
}

document.getElementById("customersTable").addEventListener("click", async (e) => {
  const editId = e.target.dataset.editCustomer;
  const delId = e.target.dataset.delCustomer;
  if (editId) {
    const c = state.customers.find((x) => String(x.id) === editId);
    const form = document.getElementById("customerForm");
    form.id.value = c.id;
    form.name.value = c.name;
    form.age.value = c.age;
    form.email.value = c.email || "";
    form.phone.value = c.phone || "";
    form.licenseNumber.value = c.licenseNumber || "";
    document.getElementById("customerFormTitle").textContent = "Edit customer";
  }
  if (delId) {
    if (!confirm("Delete this customer?")) return;
    try { await api(`/api/customers/${delId}`, { method: "DELETE" }); toast("Customer deleted"); await loadAll(); }
    catch (err) { toast(err.message, true); }
  }
});

// ---------- vehicles ----------
function renderVehicles() {
  document.getElementById("vehiclesTable").innerHTML = state.vehicles.length
    ? state.vehicles.map((v) => `
      <tr>
        <td>${esc(v.licensePlate)}</td>
        <td>${esc(v.vehicleType)}</td>
        <td>${esc(v.category)}</td>
        <td class="num">${money(v.rentalPricePerDay)}</td>
        <td><span class="badge ${v.available ? "available" : "rented"}">${v.available ? "Available" : "Rented"}</span></td>
        <td><div class="row-actions">
          <button class="icon-btn" data-edit-vehicle="${v.id}">Edit</button>
          <button class="icon-btn danger" data-del-vehicle="${v.id}">Delete</button>
        </div></td>
      </tr>`).join("")
    : `<tr><td colspan="6" class="empty">No vehicles yet</td></tr>`;
}

document.getElementById("vehicleForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const payload = {
    licensePlate: form.licensePlate.value.trim(),
    vehicleType: form.vehicleType.value,
    category: form.category.value.trim(),
    rentalPricePerDay: Number(form.rentalPricePerDay.value),
  };
  const id = form.id.value;
  try {
    await api(id ? `/api/vehicles/${id}` : "/api/vehicles", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload),
    });
    toast(id ? "Vehicle updated" : "Vehicle added");
    resetVehicleForm();
    await loadAll();
  } catch (err) { toast(err.message, true); }
});

document.getElementById("vehicleReset").addEventListener("click", resetVehicleForm);

function resetVehicleForm() {
  const form = document.getElementById("vehicleForm");
  form.reset();
  form.id.value = "";
  document.getElementById("vehicleFormTitle").textContent = "Add vehicle";
}

document.getElementById("vehiclesTable").addEventListener("click", async (e) => {
  const editId = e.target.dataset.editVehicle;
  const delId = e.target.dataset.delVehicle;
  if (editId) {
    const v = state.vehicles.find((x) => String(x.id) === editId);
    const form = document.getElementById("vehicleForm");
    form.id.value = v.id;
    form.licensePlate.value = v.licensePlate;
    form.vehicleType.value = v.vehicleType;
    form.category.value = v.category || "";
    form.rentalPricePerDay.value = v.rentalPricePerDay;
    document.getElementById("vehicleFormTitle").textContent = "Edit vehicle";
  }
  if (delId) {
    if (!confirm("Delete this vehicle?")) return;
    try { await api(`/api/vehicles/${delId}`, { method: "DELETE" }); toast("Vehicle deleted"); await loadAll(); }
    catch (err) { toast(err.message, true); }
  }
});

// ---------- rentals ----------
function renderRentalOptions() {
  const customerSelect = document.querySelector('#rentalForm select[name="customerId"]');
  const vehicleSelect = document.querySelector('#rentalForm select[name="vehicleId"]');
  customerSelect.innerHTML = state.customers.map((c) => `<option value="${c.id}">${esc(c.name)}</option>`).join("")
    || `<option value="">No customers</option>`;
  const available = state.vehicles.filter((v) => v.available);
  vehicleSelect.innerHTML = available.map((v) => `<option value="${v.id}">${esc(v.licensePlate)} — ${esc(v.category || v.vehicleType)} (${money(v.rentalPricePerDay)}/day)</option>`).join("")
    || `<option value="">No vehicles available</option>`;
}

function renderRentals() {
  document.getElementById("rentalsTable").innerHTML = state.rentals.length
    ? state.rentals.map((r) => {
        const statusCls = r.status === "RETURNED" ? "returned" : (r.overdue ? "overdue" : "rented");
        const statusText = r.status === "RETURNED" ? "Returned" : (r.overdue ? "Overdue" : "Active");
        const action = r.status === "ACTIVE"
          ? `<button class="icon-btn" data-return-rental="${r.id}">Return</button>` : "";
        return `<tr>
          <td>${esc(r.customer.name)}</td>
          <td>${esc(r.vehicle.licensePlate)}</td>
          <td>${esc(r.startDate)}</td>
          <td>${esc(r.dueDate)}</td>
          <td>${esc(r.returnDate || "—")}</td>
          <td class="num">${money(r.totalCost)}</td>
          <td><span class="badge ${statusCls}">${statusText}</span></td>
          <td><div class="row-actions">${action}</div></td>
        </tr>`;
      }).join("")
    : `<tr><td colspan="8" class="empty">No rentals yet</td></tr>`;
}

document.getElementById("rentalForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const payload = {
    customerId: Number(form.customerId.value),
    vehicleId: Number(form.vehicleId.value),
    days: Number(form.days.value),
  };
  if (!payload.customerId || !payload.vehicleId) { toast("Add a customer and an available vehicle first", true); return; }
  try {
    await api("/api/rentals", { method: "POST", body: JSON.stringify(payload) });
    toast("Rental created");
    await loadAll();
    loadDashboard();
  } catch (err) { toast(err.message, true); }
});

document.getElementById("rentalsTable").addEventListener("click", async (e) => {
  const id = e.target.dataset.returnRental;
  if (!id) return;
  try {
    await api(`/api/rentals/${id}/return`, { method: "POST" });
    toast("Vehicle returned");
    await loadAll();
    loadDashboard();
  } catch (err) { toast(err.message, true); }
});

// ---------- init ----------
(async function init() {
  try {
    await loadAll();
    await loadDashboard();
  } catch (err) {
    toast("Could not reach the server: " + err.message, true);
  }
})();
