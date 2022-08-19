package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * Listener interface for when a block is hovered over
 */
public interface BlockHoveredListener {
    /**
     * Handle when a block is hovered over
     * @param gameBlock block hovered over
     */
    void blockHovered(GameBlock gameBlock);

    /**
     * Handle when a cursor has left the block
     * @param gameBlock block deselected
     */
    void blockUnHovered(GameBlock gameBlock);
}
