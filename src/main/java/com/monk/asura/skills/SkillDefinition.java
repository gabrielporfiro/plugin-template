package com.monk.asura.skills;

import com.monk.asura.combo.MonkComboComponent;

import javax.annotation.Nonnull;

public enum SkillDefinition {
    INVOKE_ORB(
        "Habilidade 1 — Invocar Esfera Espiritual",
        "Invoca 1 orbe espiritual orbitando você. Máximo de 5 orbes. Efeito no corpo e na órbita.",
        "Nenhum (até o máximo de orbes)",
        "+1 orbe espiritual"
    ),
    FURY(
        "Habilidade 2 — Fúria",
        "Consome todas as esferas e libera poder devastador no personagem. Habilita o Punho de Asura.",
        "5 orbes espirituais ativos",
        "Consome 5 orbes"
    ),
    ASURA(
        "Habilidade 3 — Punho Supremo de Asura",
        "Concentra todas as esferas e desfere um golpe extremo em inimigos próximos.",
        "Fúria já ativada + 5 orbes reunidos",
        "Consome 5 orbes"
    );

    private final String displayName;
    private final String description;
    private final String baseRequirement;
    private final String consumption;

    SkillDefinition(String displayName, String description, String baseRequirement, String consumption) {
        this.displayName = displayName;
        this.description = description;
        this.baseRequirement = baseRequirement;
        this.consumption = consumption;
    }

    @Nonnull
    public String getDisplayName() {
        return displayName;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public String buildTooltip(@Nonnull MonkComboComponent state, int maxOrbs) {
        return displayName + "\n\n"
            + description + "\n\n"
            + "Requisitos: " + buildRequirement(state, maxOrbs) + "\n"
            + "Consumo: " + consumption;
    }

    @Nonnull
    private String buildRequirement(@Nonnull MonkComboComponent state, int maxOrbs) {
        return switch (this) {
            case INVOKE_ORB -> "Orbes: " + state.getOrbCount() + "/" + maxOrbs;
            case FURY -> state.getOrbCount() >= maxOrbs
                ? "Pronto (5/" + maxOrbs + " orbes)"
                : "Orbes: " + state.getOrbCount() + "/" + maxOrbs + " (precisa de 5)";
            case ASURA -> {
                if (state.getPhase() == com.monk.asura.combo.MonkComboPhase.ASURA_CHARGING) {
                    yield "Concentração em andamento…";
                }
                if (!state.isFuryUsedSinceLastAsura()) {
                    yield "Ative Fúria antes";
                }
                yield state.getOrbCount() >= maxOrbs
                    ? "Pronto para o golpe final"
                    : "Orbes: " + state.getOrbCount() + "/" + maxOrbs + " após a Fúria";
            }
        };
    }
}
