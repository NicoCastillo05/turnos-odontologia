import type { ReactElement } from 'react';

function App(): ReactElement {
  return (
    <main className="page">
      <div className="card">
        <h1>Turnos Odontología</h1>
        <p>Bienvenido. Ingresá a tu agenda.</p>
        <a href="/login" className="btn btn-primary">Ir al login</a>
      </div>
    </main>
  );
}

export default App;