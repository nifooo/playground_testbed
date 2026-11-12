package app.silberkorn.core

/**
 * All user-adjustable parameters of a developed picture. Everything is
 * relative to the built-in base look, so a default [Look] is "as designed".
 */
data class Look(
    /** Exposure offset in stops, -3..3. */
    val exposure: Float = 0f,
    /** -1..1 */
    val contrast: Float = 0f,
    /** -1..1, negative pulls highlights down. */
    val highlights: Float = 0f,
    /** -1..1, positive opens shadows. */
    val shadows: Float = 0f,
    /** -1 (cool) .. 1 (warm) */
    val temperature: Float = 0f,
    /** -1 (green) .. 1 (magenta) */
    val tint: Float = 0f,
    /** -1..1 */
    val saturation: Float = 0f,
    val monochrome: Boolean = false,
    /** 0..1 */
    val grain: Float = 0.5f,
    /** 0..1 */
    val grainSize: Float = 0.4f,
    /** 0..1 */
    val halation: Float = 0.5f,
    /** Index into [Aspect.ALL]; 0 keeps the full sensor frame. */
    val aspect: Int = 0,
    /** Extra clockwise quarter turns on top of the capture orientation. */
    val quarterTurns: Int = 0,
)

data class Aspect(val label: String, val ratio: Float) {
    companion object {
        /** Ratios are long side : short side. A ratio of 0 means "uncropped". */
        val ALL = listOf(
            Aspect("Voll", 0f),
            Aspect("1:1", 1f),
            Aspect("5:4", 5f / 4f),
            Aspect("3:2", 3f / 2f),
            Aspect("16:9", 16f / 9f),
            Aspect("1,85:1", 1.85f),
            Aspect("2,39:1", 2.39f),
        )
    }
}
