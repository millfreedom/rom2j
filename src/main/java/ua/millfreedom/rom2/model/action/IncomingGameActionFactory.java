package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.enums.GameActionId;

import java.util.function.Supplier;

/**
 * Native packet-ID materialization table for decodeIncomingGameAction @005056F1.
 */
public final class IncomingGameActionFactory {
    /**
     * Java utility constructor.
     * not ported.
     */
    private IncomingGameActionFactory() {
    }

    /**
     * Native support extracted from CServerApp::decodeIncomingGameAction @005056F1 action selection and payload read.
     * Fully ported.
     */
    public static CGameAction readIncomingAction(int actionIdValue, CBufferManager client) {
        CGameAction action = create(actionIdValue);
        action.ReadPayload(client);
        applyUnitOrderMode(action);
        return action;
    }

    /**
     * Native support extracted from decodeIncomingGameAction @005056F1.
     * Fully ported.
     */
    private static CGameAction create(int actionIdValue) {
        GameActionId actionId = GameActionId.fromId(actionIdValue);
        CGameAction action = actionId == null ? null : createKnownIncomingAction(actionId);
        if (action == null) {
            action = new CGameAction();
        }
        action.ID.set(actionIdValue);
        return action;
    }

    /**
     * Native support extracted from decodeIncomingGameAction @005056F1 default branch selection.
     * Fully ported.
     */
    public static boolean isKnownIncomingAction(int actionIdValue) {
        GameActionId actionId = GameActionId.fromId(actionIdValue);
        return actionId != null && incomingActionFactory(actionId) != null;
    }

    /**
     * Native support extracted from decodeIncomingGameAction @005056F1.
     * Fully ported.
     */
    private static CGameAction createKnownIncomingAction(GameActionId actionId) {
        Supplier<CGameAction> factory = incomingActionFactory(actionId);
        return factory == null ? null : factory.get();
    }

    /**
     * Native support extracted from decodeIncomingGameAction @005056F1.
     * Fully ported.
     */
    private static Supplier<CGameAction> incomingActionFactory(GameActionId actionId) {
        return switch (actionId) {
            case PLAYER_JOIN_ACTION_02 -> PlayerJoinAction::new;
            case NO_PAYLOAD_ACTION_03 -> NoPayloadAction03::new;
            case REQUEST_MAP_LOAD_ACTION_04 -> RequestMapLoadAction::new;
            case MAP_LOAD_COMPLETE_ACTION_05 -> MapLoadCompleteAction::new;
            case LOAD_SCENARIO_ACTION_06 -> LoadScenarioAction::new;
            case SAVE_GAME_REQUEST_ACTION_07 -> SaveGameRequestAction::new;
            case LOAD_GAME_REQUEST_ACTION_08 -> LoadGameRequestAction::new;
            case CLIENT_SHUTDOWN_REQUEST_ACTION_09 -> ClientShutdownRequestAction::new;
            case NO_PAYLOAD_ACTION_0A -> CGameAction::new;
            case TWO_DWORD_ACTION_0B -> MultiplayerBootstrapStatusAction::new;
            case FIXED_DWORD_ACTION_0E -> FixedDwordAction0E::new;
            case LOGIN_REQUEST_ACTION_0F -> LoginRequestAction::new;
            case STAND_STILL_ORDER_ACTION_12 -> StandStillOrderAction::new;
            case PICKUP_ALL_SACKS_ACTION_13 -> PickupAllSacksAction::new;
            case RETREAT_ORDER_ACTION_14 -> RetreatOrderAction::new;
            case MOVE_ORDER_ACTION_16 -> MoveOrderAction::new;
            case GUARD_ORDER_ACTION_17 -> GuardOrderAction::new;
            case STAND_GROUND_ORDER_ACTION_18 -> StandGroundOrderAction::new;
            case ATTACK_TARGET_ORDER_ACTION_19 -> AttackTargetOrderAction::new;
            case ATTACK_CELL_ORDER_ACTION_1A -> AttackCellOrderAction::new;
            case DEFEND_TARGET_ORDER_ACTION_1B -> DefendTargetOrderAction::new;
            case UNIT_TOKEN_LIST_ACTION_1C -> UnitTokenListAction::new;
            case PATROL_ORDER_ACTION_1D -> PatrolOrderAction::new;
            case CAST_SPELL_AT_UNIT_ACTION_1E -> CastSpellAtUnitAction::new;
            case CAST_SPELL_AT_POINT_ACTION_1F -> CastSpellAtPointAction::new;
            case PICKUP_ORDER_ACTION_21 -> PickupOrderAction::new;
            case INVENTORY_TRANSFER_ACTION_22 -> InventoryTransferAction::new;
            case DROP_GOLD_ACTION_23 -> DropGoldAction::new;
            case ENTER_TOWN_ORDER_ACTION_24 -> EnterTownOrderAction::new;
            case CANCEL_UNIT_SPELL_EFFECT_ACTION_25 -> CancelUnitSpellEffectAction::new;
            case CANCEL_POINT_SPELL_EFFECT_ACTION_26 -> CancelPointSpellEffectAction::new;
            case OPEN_SHOP_DIALOG_ACTION_32 -> OpenShopDialogAction::new;
            case SHOP_BUY_ACTION_33 -> ShopBuyAction::new;
            case SHOP_SELL_ACTION_34 -> ShopSellAction::new;
            case UNDO_SHOP_ACTION_35 -> UndoShopAction::new;
            case CLOSE_SHOP_DIALOG_ACTION_36 -> CloseShopDialogAction::new;
            case ENTER_INN_ACTION_38 -> EnterInnAction::new;
            case MAP_CHUNK_TRANSFER_COMPLETE_ACTION_39 -> MapChunkTransferCompleteAction::new;
            case LEAVE_INN_ACTION_3A -> LeaveInnAction::new;
            case REQUEST_MAP_CHUNK_ACTION_3B -> RequestMapChunkAction::new;
            case TWO_DWORD_ACTION_3D -> TwoDwordAction::new;
            case ADJUST_PLAYER_GOLD_ACTION_3E -> AdjustPlayerGoldAction::new;
            case REFRESH_SHOP_SHELVES_ACTION_3F -> RefreshShopShelvesAction::new;
            case TWO_DWORD_ACTION_40 -> TwoDwordAction::new;
            case UPDATE_DIPLOMACY_RELATIONS_ACTION_45 -> UpdateDiplomacyRelationsAction::new;
            case UPDATE_BATTLE_PREFERENCE_ACTION_46 -> UpdateBattlePreferenceAction::new;
            case SUBMIT_CHARACTER_SETUP_ACTION_48 -> SubmitCharacterSetupAction::new;
            case NAMED_CHARACTER_SPAWN_REQUEST_ACTION_49 -> NamedCharacterSpawnRequestAction::new;
            case REQUEST_PLAYER_STATE_RESYNC_ACTION_4A -> RequestPlayerStateResyncAction::new;
            case RETURN_AFTER_DEATH_ACTION_4B -> ReturnAfterDeathAction::new;
            case REVIVE_STUCK_HERO_ACTION_4C -> ReviveStuckHeroAction::new;
            case NEW_SEGMENT_ACTION_64 -> NewSegmentAction::new;
            case TWO_DWORD_ACTION_65 -> TwoDwordAction::new;
            case MONEY_ACTION_67 -> MoneyAction::new;
            case UNIT_SHOWN_ACTION_69 -> UnitShownAction::new;
            case SACK_REMOVED_ACTION_6A -> SackRemovedAction::new;
            case UNIT_MOVE_ACTION_6B -> UnitMoveAction::new;
            case UNIT_CHANGE_ACTION_6C -> UnitChangeAction_6C::new;
            case UNIT_TURN_ACTION_6D -> UnitTurnAction::new;
            case UNIT_CHANGE_ACTION_6E -> UnitChangeAction_6E::new;
            case UNIT_CHANGE_ACTION_6F -> UnitChangeAction_6F::new;
            case UNIT_CHANGE_ACTION_70 -> UnitChangeAction_70::new;
            case UNIT_ATTACK_ACTION_71 -> UnitAttackAction::new;
            case RANGED_ATTACK_ACTION_72 -> RangedAttackAction::new;
            case UNIT_DAMAGED_ACTION_73 -> UnitDamagedAction::new;
            case UNIT_HIDDEN_ACTION_74 -> UnitHiddenAction::new;
            case ITEM_LIST_ACTION_76 -> () -> ItemListAction.global;
            case SACK_ACTION_7A -> SackAction::new;
            case BUILDING_HEALTH_ACTION_82 -> BuildingHealthAction::new;
            case SHOW_SHOP_DIALOG_ACTION_83 -> ShowShopDialogAction::new;
            case SHOW_INN_DIALOG_ACTION_84 -> ShowInnDialogAction::new;
            case EFFECT_ACTION_86 -> EffectAction::new;
            case AREA_EFFECT_ACTION_87 -> AreaEffectAction::new;
            case EFFECT_HANG_ACTION_88 -> EffectHangAction::new;
            case EFFECT_GONE_ACTION_89 -> EffectGoneAction::new;
            case EFFECT_MULTI_TARGET_ACTION_8A -> EffectMultiTargetAction::new;
            case EFFECT_FROM_ACTION_8B -> EffectFromAction::new;
            case EFFECT_MULTI_FROM_ACTION_8C -> EffectMultiFromAction::new;
            case CHAT_TEXT_ACTION_91 -> ChatTextAction::new;
            case GAME_EVENT_NOTIFICATION_ACTION_92 -> GameEventNotificationAction::new;
            case PLAYER_KICKED_ACTION_93 -> PlayerKickedAction::new;
            case PLAYER_KILL_ANNOUNCEMENT_ACTION_94 -> PlayerKillAnnouncementAction::new;
            case NEW_PLAYER_ACTION_96 -> NewPlayerAction::new;
            case DELETE_PLAYER_ACTION_97 -> DeletePlayerAction::new;
            case TILE_VISIBILITY_MASK_ACTION_9B -> TileVisibilityMaskAction::new;
            case ENEMY_EQUIPMENT_ACTION_9C -> EnemyEquipmentAction::new;
            case TWO_DWORD_ACTION_AA -> MapLightOverrideAction::new;
            case SET_CAMERA_POSITION_ACTION_AB -> SetCameraPositionAction::new;
            case TWO_DWORD_ACTION_AC -> TwoDwordAction::new;
            case TWO_DWORD_ACTION_AD -> TwoDwordAction::new;
            case SELECT_MULTIPLAYER_MAP_ACTION_AE -> SelectMultiplayerMapAction::new;
            case SERVER_CLOSED_ACTION_AF -> ServerClosedAction::new;
            case PLAYER_KNOWLEDGE_PROGRESS_ACTION_B3 -> PlayerKnowledgeProgressAction::new;
            case MISSION_FAILED_ACTION_B4 -> MissionFailedAction::new;
            case MISSION_COMPLETE_ACTION_B5 -> MissionCompleteAction::new;
            case QUEST_OBJECTIVES_QUERY_OPEN_ACTION_B6 -> QuestObjectivesQueryOpenAction::new;
            case MISSION_STARTED_ACTION_B7 -> MissionStartedAction::new;
            case RETURN_TO_LOBBY_ACTION_B8 -> ReturnToLobbyAction::new;
            case DIPLOMACY_ACTION_B9 -> DiplomacyAction::new;
            case CREATURE_KNOWLEDGE_ACTION_BA -> CreatureKnowledgeAction::new;
            case PLAYER_QUESTS_ACTION_BB -> PlayerQuestsAction::new;
            case INN_QUESTS_ACTION_BC -> InnQuestsAction::new;
            case UPLOAD_CHARACTER_FILE_ACTION_BE -> UploadCharacterFileAction::new;
            case MAP_CHECKSUM_ACTION_BF -> MapChecksumAction::new;
            case MAP_CHUNK_ACTION_C0 -> MapChunkAction::new;
            case LATENCY_SETTING_ACTION_C1 -> LatencySettingAction::new;
            case MAP_TRANSFER_REDIRECT_ACTION_C2 -> MapTransferRedirectAction::new;
            case MAP_TRANSFER_TOKEN_ACTION_C3 -> MapTransferTokenAction::new;
            case MAP_TRANSFER_SPOT_VISUAL_ACTION_C4 -> PointProjectileVisualAction::new;
            case MAP_TRANSFER_BUILDING_ACTION_C5 -> MapTransferBuildingAction::new;
            default -> null;
        };
    }

    /**
     * Native support extracted from decodeIncomingGameAction @005056F1 unit-token-list cases.
     * Fully ported.
     */
    private static void applyUnitOrderMode(CGameAction action) {
        GameActionId actionId = GameActionId.fromId(action.ID.get());
        if (actionId == null) {
            return;
        }
        switch (actionId) {
            case STAND_STILL_ORDER_ACTION_12,
                 PICKUP_ALL_SACKS_ACTION_13,
                 RETREAT_ORDER_ACTION_14,
                 GUARD_ORDER_ACTION_17,
                 STAND_GROUND_ORDER_ACTION_18 -> action.unitOrderMode.set(1);
            case MOVE_ORDER_ACTION_16,
                 ATTACK_CELL_ORDER_ACTION_1A,
                 UNIT_TOKEN_LIST_ACTION_1C,
                 PATROL_ORDER_ACTION_1D,
                 CAST_SPELL_AT_POINT_ACTION_1F,
                 PICKUP_ORDER_ACTION_21,
                 ENTER_TOWN_ORDER_ACTION_24,
                 CANCEL_POINT_SPELL_EFFECT_ACTION_26 -> action.unitOrderMode.set(2);
            case ATTACK_TARGET_ORDER_ACTION_19,
                 DEFEND_TARGET_ORDER_ACTION_1B,
                 CAST_SPELL_AT_UNIT_ACTION_1E,
                 CANCEL_UNIT_SPELL_EFFECT_ACTION_25 -> action.unitOrderMode.set(3);
            default -> {
            }
        }
    }
}
