import { useMemo, useState, type ChangeEvent, type FocusEvent, type FormEvent } from 'react';
import type { CampoFormulario, DadosEmprestimo, ValoresFormulario } from '../types';
import { mascararValor, sanitizarTaxa } from '../utils/formatacao';
import { paraDadosEmprestimo, validarFormulario } from '../utils/validacao';
import { Campo } from './Campo';

interface FormularioEmprestimoProps {
  calculando: boolean;
  onCalcular: (dados: DadosEmprestimo) => void;
}

const VALORES_INICIAIS: ValoresFormulario = {
  dataInicial: '',
  dataFinal: '',
  primeiroPagamento: '',
  valorEmprestimo: '',
  taxaJuros: '',
};

const MASCARAS: Partial<Record<CampoFormulario, (valor: string) => string>> = {
  valorEmprestimo: mascararValor,
  taxaJuros: sanitizarTaxa,
};

export function FormularioEmprestimo({ calculando, onCalcular }: FormularioEmprestimoProps) {
  const [valores, setValores] = useState<ValoresFormulario>(VALORES_INICIAIS);
  const [tocados, setTocados] = useState<Partial<Record<CampoFormulario, boolean>>>({});

  const erros = useMemo(() => validarFormulario(valores), [valores]);
  const valido = Object.keys(erros).length === 0;
  const erroVisivel = (campo: CampoFormulario) => (tocados[campo] ? erros[campo] : undefined);

  function handleChange(evento: ChangeEvent<HTMLInputElement>) {
    const campo = evento.target.name as CampoFormulario;
    const mascara = MASCARAS[campo];
    const valor = mascara ? mascara(evento.target.value) : evento.target.value;
    setValores((anteriores) => ({ ...anteriores, [campo]: valor }));
  }

  function handleBlur(evento: FocusEvent<HTMLInputElement>) {
    const campo = evento.target.name as CampoFormulario;
    setTocados((anteriores) => (anteriores[campo] ? anteriores : { ...anteriores, [campo]: true }));
  }

  function handleSubmit(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (valido && !calculando) {
      onCalcular(paraDadosEmprestimo(valores));
    }
  }

  const propsComuns = { onChange: handleChange, onBlur: handleBlur };

  return (
    <form className="formulario" onSubmit={handleSubmit} noValidate aria-label="Dados do empréstimo">
      <Campo
        id="dataInicial"
        rotulo="Data Inicial"
        type="date"
        value={valores.dataInicial}
        erro={erroVisivel('dataInicial')}
        {...propsComuns}
      />
      <Campo
        id="dataFinal"
        rotulo="Data Final"
        type="date"
        min={valores.dataInicial || undefined}
        value={valores.dataFinal}
        erro={erroVisivel('dataFinal')}
        {...propsComuns}
      />
      <Campo
        id="primeiroPagamento"
        rotulo="Primeiro Pagamento"
        type="date"
        min={valores.dataInicial || undefined}
        max={valores.dataFinal || undefined}
        value={valores.primeiroPagamento}
        erro={erroVisivel('primeiroPagamento')}
        {...propsComuns}
      />
      <Campo
        id="valorEmprestimo"
        rotulo="Valor de Empréstimo"
        type="text"
        inputMode="numeric"
        autoComplete="off"
        placeholder="0,00"
        value={valores.valorEmprestimo}
        erro={erroVisivel('valorEmprestimo')}
        {...propsComuns}
      />
      <Campo
        id="taxaJuros"
        rotulo="Taxa de Juros"
        type="text"
        inputMode="decimal"
        autoComplete="off"
        placeholder="0"
        sufixo="%"
        value={valores.taxaJuros}
        erro={erroVisivel('taxaJuros')}
        {...propsComuns}
      />
      <button className="formulario__botao" type="submit" disabled={!valido || calculando}>
        {calculando ? 'Calculando…' : 'Calcular'}
      </button>
    </form>
  );
}
