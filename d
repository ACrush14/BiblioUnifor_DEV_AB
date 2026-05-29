[33mcommit b06dddc1d37d886237e8daf2599226446bf9fef0[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mdev-AndersonV11[m[33m, [m[1;31morigin/dev-AndersonV11[m[33m)[m
Author: Anderson (Crush) de Lima <Anderson.link.crush@hotmail.com>
Date:   Fri May 29 18:24:56 2026 -0300

    fix: hidrata dados de aluguel e corrige capas do ADM

[33mcommit 179ca6d0604b44a8933b884e383c604c5ace937d[m
Author: Anderson (Crush) de Lima <Anderson.link.crush@hotmail.com>
Date:   Fri May 29 18:23:03 2026 -0300

    Melhoria na gestão de amizades, fluxo de aluguel físico e estabilidade de uploads
    
    Este commit implementa melhorias críticas na rede social (amigos) e no fluxo de gestão de aluguéis para administradores, além de otimizar o carregamento de imagens e a robustez de operações com o Firebase.
    
    - **Módulo de Amizades (RF17):**
        - **Sincronização em Tempo Real:** Substitui chamadas estáticas por `addSnapshotListener` na lista de amigos e solicitações, garantindo atualização imediata da UI.
        - **Gestão de Vínculos:** Implementa funcionalidade de "Desfazer Amizade" e adiciona verificação de estado (pendente/amigo) ao visualizar perfis de terceiros.
        - **Estabilidade:** Adiciona proteções (`try-catch` e verificações de ciclo de vida) contra crashes do Google Play Services (`Phenotype.API`) durante execuções de `writeBatch` em emuladores.
        - **Performance:** Implementa debounce de 500ms na busca de usuários e limita resultados iniciais a 30 documentos para reduzir o consumo de dados.
    
    - **Gestão Administrativa (RF36):**
        - **Fluxo de Aluguel Físico:** Adiciona botões contextuais no `AlugueisAdapter` para "Aprovar Aluguel" (gera prazo de 14 dias) e "Receber Livro".
        - **Estoque Atômico:** A devolução de livros agora utiliza `FieldValue.increment(1)` para atualizar o acervo de forma atômica no Firestore, evitando condições de corrida.
        - **Navegação:** Integração direta com as telas de informações do livro (RF37) e do usuário (RF30) a partir da lista de aluguéis.
    
    - **Imagens e Performance:**
        - **Padronização de Placeholders:** Substitui recursos antigos pelo novo `ic_sem_capa` e `user_placeholder` em todos os adaptadores.
        - **Otimização de Memória:** Configura o Coil para redimensionar avatares (máx 200dp) e utiliza `crossfade` para transições suaves em RecyclerViews.
        - **Upload Robusto:** Reformula o envio de foto de perfil utilizando `putStream` (em vez de `putBytes` ou `putFile`), resolvendo problemas de expiração de URI no Android 10+ e garantindo a persistência da `fotoUrl` no Firestore.
    
    - **Infraestrutura:**
        - Ativa persistência offline do Firestore na `TelaRF17Amigos`.
        - Expande o modelo `ItemLivraria` e `ItemAluguel` para suportar URLs de capa e datas de devolução calculadas.
        - Adiciona novas strings de feedback e botões ao `strings.xml`.

[33mcommit 0c1ff35aad0a1239c629e0a1d3c43be2cd000750[m
Author: Anderson (Crush) de Lima <Anderson.link.crush@hotmail.com>
Date:   Fri May 29 18:08:31 2026 -0300

    Melhoria na gestão de amizades, fluxo de aluguel físico e estabilidade de uploads
    
    Este commit implementa melhorias críticas na rede social (amigos) e no fluxo de gestão de aluguéis para administradores, além de otimizar o carregamento de imagens e a robustez de operações com o Firebase.
    
    - **Módulo de Amizades (RF17):**
        - **Sincronização em Tempo Real:** Substitui chamadas estáticas por `addSnapshotListener` na lista de amigos e solicitações, garantindo atualização imediata da UI.
        - **Gestão de Vínculos:** Implementa funcionalidade de "Desfazer Amizade" e adiciona verificação de estado (pendente/amigo) ao visualizar perfis de terceiros.
        - **Estabilidade:** Adiciona proteções (`try-catch` e verificações de ciclo de vida) contra crashes do Google Play Services (`Phenotype.API`) durante execuções de `writeBatch` em emuladores.
        - **Performance:** Implementa debounce de 500ms na busca de usuários e limita resultados iniciais a 30 documentos para reduzir o consumo de dados.
    
    - **Gestão Administrativa (RF36):**
        - **Fluxo de Aluguel Físico:** Adiciona botões contextuais no `AlugueisAdapter` para "Aprovar Aluguel" (gera prazo de 14 dias) e "Receber Livro".
        - **Estoque Atômico:** A devolução de livros agora utiliza `FieldValue.increment(1)` para atualizar o acervo de forma atômica no Firestore, evitando condições de corrida.
        - **Navegação:** Integração direta com as telas de informações do livro (RF37) e do usuário (RF30) a partir da lista de aluguéis.
    
    - **Imagens e Performance:**
        - **Padronização de Placeholders:** Substitui recursos antigos pelo novo `ic_sem_capa` e `user_placeholder` em todos os adaptadores.
        - **Otimização de Memória:** Configura o Coil para redimensionar avatares (máx 200dp) e utiliza `crossfade` para transições suaves em RecyclerViews.
        - **Upload Robusto:** Reformula o envio de foto de perfil utilizando `putStream` (em vez de `putBytes` ou `putFile`), resolvendo problemas de expiração de URI no Android 10+ e garantindo a persistência da `fotoUrl` no Firestore.
    
    - **Infraestrutura:**
        - Ativa persistência offline do Firestore na `TelaRF17Amigos`.
        - Expande o modelo `ItemLivraria` e `ItemAluguel` para suportar URLs de capa e datas de devolução calculadas.
        - Adiciona novas strings de feedback e botões ao `strings.xml`.

[33mcommit 289cdc63bbaf1f7e16275bd9cf48ccdc7288bd1c[m
Merge: 7410ac5 28f3193
Author: Brena Lemos <brenavitorialemos@gmail.com>
Date:   Fri May 29 16:25:49 2026 -0300

    Merge pull request #51 from brenalemos09/dev-BrenaV09
    
    Summarize the changes as:

[33mcommit 28f31936a4501849c0ec02dac1d28a2293dd5a1a[m
Author: Brena Lemos <brenavitorialemos@gmail.com>
Date:   Fri May 29 16:25:17 2026 -0300

    Summarize the changes as:
    
    ```
    refactor: improve UI performance and activity lifecycle safety
    
    - Optimized multiple screens (Wishlist, Library, Admin Search, etc.) by replacing NestedScrollView with direct RecyclerView implementations to enable view recycling.
    - Implemented `activeDialog` management across all Activities to prevent memory leaks and "WindowManager: bad token" crashes during destruction.
    - Added `isFinishing` and `isDestroyed` checks to Firebase and Coroutine callbacks to prevent background updates on dead activities.
    - Enhanced Admin "Solicitações" and "Aluguéis" screens with local filtering and search bar functionality.
    - Added password re-authentication to the Admin password reset flow (TelaRF39).
    - Cleaned up UI layouts by removing hardcoded strings, fixing constraints, and improving accessibility descriptions.
