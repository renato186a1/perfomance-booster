# Performance Booster

Aplicativo Android nativo (Kotlin) para diagnóstico e ajustes controlados via **Shizuku**, com scripts opcionais para **ADB pelo computador**. A compilação é feita no GitHub Actions.

## O que está incluído

- Painel Android escuro e simples.
- Verificação e solicitação de permissão Shizuku.
- Diagnóstico de modelo, versão Android, uptime, armazenamento e memória.
- Ajustes reversíveis de escala de animações (0x, 0,5x e 1x).
- Compilação ART `speed-profile` para um pacote informado.
- Comando de manutenção `pm trim-caches` para caches recuperáveis do sistema.
- Scripts Windows para diagnóstico e animações via ADB USB.

## Requisitos

- Android 8.0 (API 26) ou superior.
- Shizuku instalado e iniciado no aparelho para os recursos dentro do app.
- Para o caminho ADB pelo PC: Android Platform Tools, depuração USB e autorização do computador.
- Para compilar: conta GitHub. Não é necessário Android Studio.

## Compilar no GitHub

1. Crie um repositório GitHub vazio chamado `PerformanceBooster`.
2. Extraia este ZIP e envie todos os arquivos para a raiz do repositório.
3. Abra **Actions** e habilite workflows, se solicitado.
4. Faça um `push` para `main`/`master` ou abra **Actions → Android CI - Performance Booster → Run workflow**.
5. Ao terminar, abra a execução e baixe o artefato `Performance-Booster-debug`.
6. Instale o APK no Android. Para distribuição, configure assinatura de release (ver abaixo).

## Usar Shizuku

1. Instale o Shizuku pela fonte oficial: https://shizuku.rikka.app/
2. Inicie o Shizuku usando depuração sem fio (opção sem root) ou root.
3. Abra Performance Booster e toque em **Conectar / autorizar Shizuku**.
4. Confirme a autorização na tela do Shizuku.
5. Execute primeiro o diagnóstico. Aplique apenas ajustes que entender e teste o comportamento.

O Shizuku não concede root: ele disponibiliza uma interface de execução com permissões equivalentes ao serviço que o iniciou. Algumas operações variam conforme fabricante e versão Android.

## Usar ADB pelo PC

1. Instale Android SDK Platform Tools: https://developer.android.com/tools/releases/platform-tools
2. Ative Opções do desenvolvedor → Depuração USB.
3. Conecte o aparelho e aceite a autorização RSA.
4. No Windows, execute `adb/adb-optimize-windows.bat`.
5. Para reverter escalas, execute `adb/restore-defaults-windows.bat`.

## Assinatura de APK Release

O workflow gera um APK release **não assinado**. Para instalar em aparelhos de teste, use o APK debug, que é assinado automaticamente pelo Android Gradle Plugin. Para publicar/distribuir, crie uma chave de assinatura e configure `signingConfigs` com GitHub Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). Nunca coloque keystore ou senhas no repositório.

## Segurança e limites

- Não existe um botão mágico que aumente FPS em todos os aparelhos. O desempenho depende do SoC, temperatura, memória, firmware e jogo.
- O app não força overclock, não altera governor/kernel, não mata processos em massa e não desativa serviços de segurança.
- Escalas de animação mudam a sensação de velocidade da interface, não a potência real da CPU/GPU.
- `speed-profile` solicita compilação ART guiada por perfil; não garante melhora em jogos.
- `pm trim-caches` pede ao Android para liberar caches recuperáveis; não apaga dados pessoais nem equivale a limpar dados de apps.
- Faça backup antes de experimentar ajustes de sistema. Alguns comandos podem não ser aceitos em certos fabricantes/versões.
