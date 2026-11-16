import { useCallback, useEffect, useRef, useState } from 'react';
import { FormularioEmprestimo } from './components/FormularioEmprestimo';
import { calcularEmprestimo, ErroCalculo } from './services/calculadoraApi';
import type { DadosEmprestimo, LinhaCalculo } from './types';

const MENSAGEM_FALHA_CONEXAO = 'Não foi possível conectar ao servidor. Tente novamente.';

function App() {
  const [linhas, setLinhas] = useState<LinhaCalculo[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [calculando, setCalculando] = useState(false);
  const requisicaoAtual = useRef<AbortController | null>(null);

  useEffect(() => () => requisicaoAtual.current?.abort(), []);

  const handleCalcular = useCallback(async (dados: DadosEmprestimo) => {
    requisicaoAtual.current?.abort();
    const controlador = new AbortController();
    requisicaoAtual.current = controlador;

    setCalculando(true);
    setErro(null);
    try {
      setLinhas(await calcularEmprestimo(dados, controlador.signal));
    } catch (falha) {
      if (controlador.signal.aborted) {
        return;
      }
      setLinhas(null);
      setErro(falha instanceof ErroCalculo ? falha.message : MENSAGEM_FALHA_CONEXAO);
    } finally {
      if (requisicaoAtual.current === controlador) {
        setCalculando(false);
      }
    }
  }, []);

  return (
    <main className="pagina">
      <h1 className="pagina__titulo">Calculadora de Empréstimos</h1>
      <FormularioEmprestimo calculando={calculando} onCalcular={handleCalcular} />

      <p className="pagina__status" role="status" aria-live="polite">
        {linhas && !calculando ? `Cálculo concluído: ${linhas.length} linhas.` : ''}
      </p>
      {erro && (
        <p className="pagina__erro" role="alert">
          {erro}
        </p>
      )}
    </main>
  );
}

export default App;
