import type { InputHTMLAttributes } from 'react';
import type { CampoFormulario } from '../types';

interface CampoProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> {
  id: CampoFormulario;
  rotulo: string;
  erro?: string;
  sufixo?: string;
}

export function Campo({ id, rotulo, erro, sufixo, ...propsInput }: CampoProps) {
  const idErro = `${id}-erro`;
  return (
    <div className="campo">
      <label className="campo__rotulo" htmlFor={id}>
        {rotulo}
      </label>
      <div className={`campo__entrada${erro ? ' campo__entrada--erro' : ''}`}>
        <input
          id={id}
          name={id}
          required
          aria-invalid={erro ? true : undefined}
          aria-describedby={erro ? idErro : undefined}
          {...propsInput}
        />
        {sufixo && (
          <span className="campo__sufixo" aria-hidden="true">
            {sufixo}
          </span>
        )}
      </div>
      {erro && (
        <p id={idErro} className="campo__erro">
          {erro}
        </p>
      )}
    </div>
  );
}
