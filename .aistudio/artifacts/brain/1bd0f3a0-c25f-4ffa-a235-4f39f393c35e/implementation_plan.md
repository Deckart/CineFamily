# Plan d'implémentation : Administration des codes PIN et Bascule de profil avec bandeau de retour

## 1. Contexte et Objectifs
L'utilisateur souhaite que l'administrateur (Steph) puisse :
1. **Modifier les codes PIN** de n'importe quel membre de la famille directement depuis la modification de profil dans l'onglet Famille.
2. **Agir à la place d'un autre membre** (ajouter des films, noter, supprimer, etc.) via un mode de bascule temporaire de profil avec un bandeau de retour permanent pour revenir à tout moment au compte administrateur sans ressaisir de code PIN.

---

## 2. Architecture & Modifications prévues

### A. Gestion de l'état dans `MovieViewModel.kt`
- **Bascule de profil temporaire (Impersonation) :**
  - Ajouter un état `originalAdminSession: StateFlow<FamilyMember?>` conservant le profil admin lorsqu'une session d'incarnation est active.
  - Implémenter `impersonateMember(targetMember: FamilyMember)` : bascule `activeMember` vers le profil cible sans exiger de code PIN et mémorise l'administrateur actuel.
  - Implémenter `exitImpersonation()` : réactive immédiatement l'administrateur comme `activeMember` et efface l'état d'incarnation.
- **Modification complète de profil et de code PIN par l'admin :**
  - Implémenter `updateMemberByAdmin(memberId: String, newName: String, newRole: String, newPin: String, newGenres: String, newEmoji: String, newColorHex: Long)`.
  - Mettre à jour Room et synchroniser avec Firestore (`family_members`).
  - Gérer la mise à jour réactive de `activeMember` si le profil modifié est actuellement sélectionné.

### B. Bandeau de retour permanent dans l'interface (`MainActivity.kt`)
- Lorsque l'administrateur agit en tant qu'un autre membre (`originalAdminSession != null`) :
  - Afficher un bandeau d'en-tête persistant sous la barre d'action :
    - Texte clair : *"Mode Administrateur : vous agissez en tant que [Nom]"*.
    - Bouton d'action rapide : *"Revenir à Steph"* pour quitter le mode en un clic.
- Toutes les actions de l'application (ajout d'un film, notation et avis, suppression, filtrage des films "Solo") sont alors automatiquement attribuées à ce membre sans avoir à changer les autres écrans.

### C. Interface d'administration dans `FamilyScreen.kt`
- **Dialogue de modification de membre :**
  - Permettre à l'administrateur d'éditer n'importe quel profil de membre via un bouton "Modifier" sur chaque carte.
  - Champs modifiables : Nom, Rôle, **Code PIN (4 chiffres)**, Préférences de genres, Avatar et Couleur.
  - Validation et enregistrement via `updateMemberByAdmin`.
- **Action rapide "Agir en tant que [Nom]" :**
  - Bouton dédié sur la carte de chaque membre pour démarrer instantanément la bascule vers son compte.

---

## 3. Statut de réalisation
- **Modification de code PIN par l'admin :** Terminé et opérationnel dans l'onglet Famille (bouton "Modifier / PIN"). Les changements de nom, rôle, code PIN, genres, avatar et couleur sont immédiatement persistés en local (Room) et sur Firebase Firestore.
- **Mode bascule de profil temporaire (Impersonation) :** Terminé et opérationnel. Bouton "Agir en tant que [Nom]" sur chaque carte de membre et dans le dialogue de modification.
- **Bandeau de retour sticky :** Intégré dans l'en-tête persistant de l'application (`MainActivity.kt`) et dans l'écran de profil, permettant à Steph de revenir à son compte administrateur en un clic.
- **Compilation :** `compile_applet` réussi avec succès.
