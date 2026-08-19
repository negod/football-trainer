import { useEffect, useState, type DependencyList } from 'react';

type AsyncState<T> = {
  data: T | null;
  error: string | null;
  loading: boolean;
};

export function useAsync<T>(load: () => Promise<T>, dependencies: DependencyList): AsyncState<T> {
  const [state, setState] = useState<AsyncState<T>>({
    data: null,
    error: null,
    loading: true,
  });

  useEffect(() => {
    let active = true;
    setState((current) => ({ ...current, error: null, loading: true }));

    load()
      .then((data) => {
        if (active) setState({ data, error: null, loading: false });
      })
      .catch((error: unknown) => {
        if (active) {
          setState({ data: null, error: error instanceof Error ? error.message : 'Request failed', loading: false });
        }
      });

    return () => { active = false; };
  }, dependencies);

  return state;
}

