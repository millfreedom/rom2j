package ua.millfreedom.rom2.res;

/**
 * union ResValueNode
 */
public sealed interface ResValueNode extends ResNodeData
        permits ResByteNode, ResDoubleNode, ResIntNode, ResShortNode {
}
