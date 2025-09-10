# Barbearia do Recruta Zero (2025) ✂️
**Trabalho Prático II — Sistemas Operacionais (IEC584)**  

## Descrição
Implementação do problema "Barbearia do Recruta Zero": simulação com threads e semáforos das filas por categoria (oficiais, sargentos, cabos) e geração de relatório periódico. Requisitos e enunciado conforme especificação do trabalho. :contentReference[oaicite:1]{index=1}

## Estrutura do repositório
```bash
BarbeiroDorminhocoPt2/
├─ src/
│   ├─ BarbeariaRecrutaZero.java
│   ├─ BarbeariaRecrutaZeroCasoA.java
│   ├─ BarbeariaRecrutaZeroCasoB.java
│   ├─ BarbeariaRecrutaZeroCasoC.java
├─ out/ (classes compiladas)
├─ .idea/, .iml, .classpath, .project
```


> ⚠️ Pastas como `.idea/`, `out/`, `.iml`, `.classpath` e `.project` são específicas da IDE e não precisam estar no GitHub.


## Como compilar
No terminal, dentro da pasta raiz:

```bash
# Compilar todas as classes
javac src/*.java -d bin
```
> isso criará as .class na pasta bin/.

## Como executar
Entre na pasta bin/ e rode o caso desejado:
```bash
cd bin
````

## Executar caso A
```bash
java BarbeariaRecrutaZeroCasoA
````

## Executar caso B
```bash
java BarbeariaRecrutaZeroCasoB
```

## Executar caso C
```bash
java BarbeariaRecrutaZeroCasoC
```

## Entradas de teste

O sistema pode ser adaptado para ler arquivos de entrada (clientes, categorias, tempos).  
Caso não fornecido, os clientes são gerados aleatoriamente em memória.  

### Exemplo (`data/sample_input.txt`):
```txt
# Formato: categoria tempo
# categorias: 1 = Oficial, 2 = Sargento, 3 = Cabo, 0 = Pausa

1 5
2 3
3 2
3 1
2 4
0 0
1 6
3 2
2 3
3 1
```
## 📄 Licença
Este projeto foi desenvolvido por **Beatriz Christine Azevedo Batista**  
e está licenciado sob a **Licença MIT** — veja o arquivo [LICENSE](LICENSE) para mais detalhes.
