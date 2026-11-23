package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

private var _air: ImageVector? = null

val Icons.Filled.Air: ImageVector
    get() {
        _air?.let { return it }
        val built = materialIcon(name = "Filled.Air") {
            materialPath {
                moveTo(14.5f, 17.0f)
                curveToRelative(0.0f, 1.65f, -1.35f, 3.0f, -3.0f, 3.0f)
                reflectiveCurveToRelative(-3.0f, -1.35f, -3.0f, -3.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(9.5f)
                curveTo(13.15f, 14.0f, 14.5f, 15.35f, 14.5f, 17.0f)
                close()
                moveTo(19.0f, 6.5f)
                curveTo(19.0f, 4.57f, 17.43f, 3.0f, 15.5f, 3.0f)
                reflectiveCurveTo(12.0f, 4.57f, 12.0f, 6.5f)
                horizontalLineToRelative(2.0f)
                curveTo(14.0f, 5.67f, 14.67f, 5.0f, 15.5f, 5.0f)
                reflectiveCurveTo(17.0f, 5.67f, 17.0f, 6.5f)
                reflectiveCurveTo(16.33f, 8.0f, 15.5f, 8.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(13.5f)
                curveTo(17.43f, 10.0f, 19.0f, 8.43f, 19.0f, 6.5f)
                close()
                moveTo(18.5f, 11.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(16.5f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(19.33f, 16.0f, 18.5f, 16.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveTo(20.43f, 11.0f, 18.5f, 11.0f)
                close()
            }
        }
        _air = built
        return built
    }

private var _android: ImageVector? = null

val Icons.Filled.Android: ImageVector
    get() {
        _android?.let { return it }
        val built = materialIcon(name = "Filled.Android") {
            materialPath {
                moveTo(17.6f, 9.48f)
                lineToRelative(1.84f, -3.18f)
                curveToRelative(0.16f, -0.31f, 0.04f, -0.69f, -0.26f, -0.85f)
                curveToRelative(-0.29f, -0.15f, -0.65f, -0.06f, -0.83f, 0.22f)
                lineToRelative(-1.88f, 3.24f)
                curveToRelative(-2.86f, -1.21f, -6.08f, -1.21f, -8.94f, 0.0f)
                lineTo(5.65f, 5.67f)
                curveToRelative(-0.19f, -0.29f, -0.58f, -0.38f, -0.87f, -0.2f)
                curveTo(4.5f, 5.65f, 4.41f, 6.01f, 4.56f, 6.3f)
                lineTo(6.4f, 9.48f)
                curveTo(3.3f, 11.25f, 1.28f, 14.44f, 1.0f, 18.0f)
                horizontalLineToRelative(22.0f)
                curveTo(22.72f, 14.44f, 20.7f, 11.25f, 17.6f, 9.48f)
                close()
                moveTo(7.0f, 15.25f)
                curveToRelative(-0.69f, 0.0f, -1.25f, -0.56f, -1.25f, -1.25f)
                curveToRelative(0.0f, -0.69f, 0.56f, -1.25f, 1.25f, -1.25f)
                reflectiveCurveTo(8.25f, 13.31f, 8.25f, 14.0f)
                curveTo(8.25f, 14.69f, 7.69f, 15.25f, 7.0f, 15.25f)
                close()
                moveTo(17.0f, 15.25f)
                curveToRelative(-0.69f, 0.0f, -1.25f, -0.56f, -1.25f, -1.25f)
                curveToRelative(0.0f, -0.69f, 0.56f, -1.25f, 1.25f, -1.25f)
                reflectiveCurveToRelative(1.25f, 0.56f, 1.25f, 1.25f)
                curveTo(18.25f, 14.69f, 17.69f, 15.25f, 17.0f, 15.25f)
                close()
            }
        }
        _android = built
        return built
    }

private var _apps: ImageVector? = null

val Icons.Filled.Apps: ImageVector
    get() {
        _apps?.let { return it }
        val built = materialIcon(name = "Filled.Apps") {
            materialPath {
                moveTo(4.0f, 8.0f)
                horizontalLineToRelative(4.0f)
                lineTo(8.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(10.0f, 20.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(4.0f, 20.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                lineTo(4.0f, 16.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(4.0f, 14.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                lineTo(4.0f, 10.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(10.0f, 14.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(16.0f, 4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(4.0f)
                lineTo(20.0f, 4.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(10.0f, 8.0f)
                horizontalLineToRelative(4.0f)
                lineTo(14.0f, 4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(16.0f, 14.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(16.0f, 20.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        _apps = built
        return built
    }

private var _aspectRatio: ImageVector? = null

val Icons.Filled.AspectRatio: ImageVector
    get() {
        _aspectRatio?.let { return it }
        val built = materialIcon(name = "Filled.AspectRatio") {
            materialPath {
                moveTo(19.0f, 12.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-5.0f)
                close()
                moveTo(7.0f, 9.0f)
                horizontalLineToRelative(3.0f)
                lineTo(10.0f, 7.0f)
                lineTo(5.0f, 7.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(2.0f)
                lineTo(7.0f, 9.0f)
                close()
                moveTo(21.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(23.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(21.0f, 19.01f)
                lineTo(3.0f, 19.01f)
                lineTo(3.0f, 4.99f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(14.02f)
                close()
            }
        }
        _aspectRatio = built
        return built
    }

private var _autoAwesome: ImageVector? = null

val Icons.Filled.AutoAwesome: ImageVector
    get() {
        _autoAwesome?.let { return it }
        val built = materialIcon(name = "Filled.AutoAwesome") {
            materialPath {
                moveTo(19.0f, 9.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 5.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 1.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 5.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 9.0f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9.0f, 4.0f)
                lineTo(6.5f, 9.5f)
                lineTo(1.0f, 12.0f)
                lineToRelative(5.5f, 2.5f)
                lineTo(9.0f, 20.0f)
                lineToRelative(2.5f, -5.5f)
                lineTo(17.0f, 12.0f)
                lineToRelative(-5.5f, -2.5f)
                close()
                moveTo(19.0f, 15.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 19.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 23.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 19.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 15.0f)
                close()
            }
        }
        _autoAwesome = built
        return built
    }

private var _barChart: ImageVector? = null

val Icons.Filled.BarChart: ImageVector
    get() {
        _barChart?.let { return it }
        val built = materialIcon(name = "Filled.BarChart") {
            materialPath {
                moveTo(4.0f, 9.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(11.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
            materialPath {
                moveTo(16.0f, 13.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
            materialPath {
                moveTo(10.0f, 4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(16.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
        }
        _barChart = built
        return built
    }

private var _bedtime: ImageVector? = null

val Icons.Filled.Bedtime: ImageVector
    get() {
        _bedtime?.let { return it }
        val built = materialIcon(name = "Filled.Bedtime") {
            materialPath {
                moveTo(12.34f, 2.02f)
                curveTo(6.59f, 1.82f, 2.0f, 6.42f, 2.0f, 12.0f)
                curveToRelative(0.0f, 5.52f, 4.48f, 10.0f, 10.0f, 10.0f)
                curveToRelative(3.71f, 0.0f, 6.93f, -2.02f, 8.66f, -5.02f)
                curveTo(13.15f, 16.73f, 8.57f, 8.55f, 12.34f, 2.02f)
                close()
            }
        }
        _bedtime = built
        return built
    }

private var _bolt: ImageVector? = null

val Icons.Filled.Bolt: ImageVector
    get() {
        _bolt?.let { return it }
        val built = materialIcon(name = "Filled.Bolt") {
            materialPath {
                moveTo(11.0f, 21.0f)
                horizontalLineToRelative(-1.0f)
                lineToRelative(1.0f, -7.0f)
                horizontalLineTo(7.5f)
                curveToRelative(-0.58f, 0.0f, -0.57f, -0.32f, -0.38f, -0.66f)
                curveToRelative(0.19f, -0.34f, 0.05f, -0.08f, 0.07f, -0.12f)
                curveTo(8.48f, 10.94f, 10.42f, 7.54f, 13.0f, 3.0f)
                horizontalLineToRelative(1.0f)
                lineToRelative(-1.0f, 7.0f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.49f, 0.0f, 0.56f, 0.33f, 0.47f, 0.51f)
                lineToRelative(-0.07f, 0.15f)
                curveTo(12.96f, 17.55f, 11.0f, 21.0f, 11.0f, 21.0f)
                close()
            }
        }
        _bolt = built
        return built
    }

private var _brightness6: ImageVector? = null

val Icons.Filled.Brightness6: ImageVector
    get() {
        _brightness6?.let { return it }
        val built = materialIcon(name = "Filled.Brightness6") {
            materialPath {
                moveTo(20.0f, 15.31f)
                lineTo(23.31f, 12.0f)
                lineTo(20.0f, 8.69f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(-4.69f)
                lineTo(12.0f, 0.69f)
                lineTo(8.69f, 4.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(4.69f)
                lineTo(0.69f, 12.0f)
                lineTo(4.0f, 15.31f)
                verticalLineTo(20.0f)
                horizontalLineToRelative(4.69f)
                lineTo(12.0f, 23.31f)
                lineTo(15.31f, 20.0f)
                horizontalLineTo(20.0f)
                verticalLineToRelative(-4.69f)
                close()
                moveTo(12.0f, 18.0f)
                verticalLineTo(6.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, 2.69f, 6.0f, 6.0f)
                reflectiveCurveToRelative(-2.69f, 6.0f, -6.0f, 6.0f)
                close()
            }
        }
        _brightness6 = built
        return built
    }

private var _calendarMonth: ImageVector? = null

val Icons.Filled.CalendarMonth: ImageVector
    get() {
        _calendarMonth?.let { return it }
        val built = materialIcon(name = "Filled.CalendarMonth") {
            materialPath {
                moveTo(19.0f, 4.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 4.0f, 3.01f, 4.9f, 3.01f, 6.0f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(21.0f, 4.9f, 20.1f, 4.0f, 19.0f, 4.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(9.0f, 14.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(17.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(9.0f, 18.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(17.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        _calendarMonth = built
        return built
    }

private var _category: ImageVector? = null

val Icons.Filled.Category: ImageVector
    get() {
        _category?.let { return it }
        val built = materialIcon(name = "Filled.Category") {
            materialPath {
                moveTo(12.0f, 2.0f)
                lineToRelative(-5.5f, 9.0f)
                horizontalLineToRelative(11.0f)
                close()
            }
            materialPath {
                moveTo(17.5f, 17.5f)
                moveToRelative(-4.5f, 0.0f)
                arcToRelative(4.5f, 4.5f, 0.0f, true, true, 9.0f, 0.0f)
                arcToRelative(4.5f, 4.5f, 0.0f, true, true, -9.0f, 0.0f)
            }
            materialPath {
                moveTo(3.0f, 13.5f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(8.0f)
                horizontalLineTo(3.0f)
                close()
            }
        }
        _category = built
        return built
    }

private var _chevronLeft: ImageVector? = null

val Icons.Filled.ChevronLeft: ImageVector
    get() {
        _chevronLeft?.let { return it }
        val built = materialIcon(name = "Filled.ChevronLeft") {
            materialPath {
                moveTo(15.41f, 7.41f)
                lineTo(14.0f, 6.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(6.0f, 6.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(10.83f, 12.0f)
                close()
            }
        }
        _chevronLeft = built
        return built
    }

private var _chevronRight: ImageVector? = null

val Icons.Filled.ChevronRight: ImageVector
    get() {
        _chevronRight?.let { return it }
        val built = materialIcon(name = "Filled.ChevronRight") {
            materialPath {
                moveTo(10.0f, 6.0f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12.0f)
                lineToRelative(-4.58f, 4.59f)
                lineTo(10.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
        _chevronRight = built
        return built
    }

private var _cloudDownload: ImageVector? = null

val Icons.Filled.CloudDownload: ImageVector
    get() {
        _cloudDownload?.let { return it }
        val built = materialIcon(name = "Filled.CloudDownload") {
            materialPath {
                moveTo(19.35f, 10.04f)
                curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
                curveTo(9.11f, 4.0f, 6.6f, 5.64f, 5.35f, 8.04f)
                curveTo(2.34f, 8.36f, 0.0f, 10.91f, 0.0f, 14.0f)
                curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                close()
                moveTo(17.0f, 13.0f)
                lineToRelative(-5.0f, 5.0f)
                lineToRelative(-5.0f, -5.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(3.0f)
                close()
            }
        }
        _cloudDownload = built
        return built
    }

private var _colorLens: ImageVector? = null

val Icons.Filled.ColorLens: ImageVector
    get() {
        _colorLens?.let { return it }
        val built = materialIcon(name = "Filled.ColorLens") {
            materialPath {
                moveTo(12.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                reflectiveCurveToRelative(4.03f, 9.0f, 9.0f, 9.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                curveToRelative(0.0f, -0.39f, -0.15f, -0.74f, -0.39f, -1.01f)
                curveToRelative(-0.23f, -0.26f, -0.38f, -0.61f, -0.38f, -0.99f)
                curveToRelative(0.0f, -0.83f, 0.67f, -1.5f, 1.5f, -1.5f)
                lineTo(16.0f, 16.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -4.42f, -4.03f, -8.0f, -9.0f, -8.0f)
                close()
                moveTo(6.5f, 12.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(5.67f, 9.0f, 6.5f, 9.0f)
                reflectiveCurveTo(8.0f, 9.67f, 8.0f, 10.5f)
                reflectiveCurveTo(7.33f, 12.0f, 6.5f, 12.0f)
                close()
                moveTo(9.5f, 8.0f)
                curveTo(8.67f, 8.0f, 8.0f, 7.33f, 8.0f, 6.5f)
                reflectiveCurveTo(8.67f, 5.0f, 9.5f, 5.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(10.33f, 8.0f, 9.5f, 8.0f)
                close()
                moveTo(14.5f, 8.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(13.67f, 5.0f, 14.5f, 5.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(15.33f, 8.0f, 14.5f, 8.0f)
                close()
                moveTo(17.5f, 12.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(16.67f, 9.0f, 17.5f, 9.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }
        _colorLens = built
        return built
    }

private var _contentPaste: ImageVector? = null

val Icons.Filled.ContentPaste: ImageVector
    get() {
        _contentPaste?.let { return it }
        val built = materialIcon(name = "Filled.ContentPaste") {
            materialPath {
                moveTo(19.0f, 2.0f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 0.84f, 13.3f, 0.0f, 12.0f, 0.0f)
                curveToRelative(-1.3f, 0.0f, -2.4f, 0.84f, -2.82f, 2.0f)
                lineTo(5.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(19.0f, 20.0f)
                lineTo(5.0f, 20.0f)
                lineTo(5.0f, 4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(10.0f)
                lineTo(17.0f, 4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(16.0f)
                close()
            }
        }
        _contentPaste = built
        return built
    }

private var _deleteOutline: ImageVector? = null

val Icons.Filled.DeleteOutline: ImageVector
    get() {
        _deleteOutline?.let { return it }
        val built = materialIcon(name = "Filled.DeleteOutline") {
            materialPath {
                moveTo(6.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(18.0f, 7.0f)
                lineTo(6.0f, 7.0f)
                verticalLineToRelative(12.0f)
                close()
                moveTo(8.0f, 9.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(10.0f)
                lineTo(8.0f, 19.0f)
                lineTo(8.0f, 9.0f)
                close()
                moveTo(15.5f, 4.0f)
                lineToRelative(-1.0f, -1.0f)
                horizontalLineToRelative(-5.0f)
                lineToRelative(-1.0f, 1.0f)
                lineTo(5.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                lineTo(19.0f, 4.0f)
                close()
            }
        }
        _deleteOutline = built
        return built
    }

private var _deleteSweep: ImageVector? = null

val Icons.Filled.DeleteSweep: ImageVector
    get() {
        _deleteSweep?.let { return it }
        val built = materialIcon(name = "Filled.DeleteSweep") {
            materialPath {
                moveTo(15.0f, 16.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(15.0f, 8.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-7.0f)
                close()
                moveTo(15.0f, 12.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                close()
                moveTo(3.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(13.0f, 8.0f)
                lineTo(3.0f, 8.0f)
                verticalLineToRelative(10.0f)
                close()
                moveTo(14.0f, 5.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-1.0f, -1.0f)
                lineTo(6.0f, 4.0f)
                lineTo(5.0f, 5.0f)
                lineTo(2.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.0f)
                close()
            }
        }
        _deleteSweep = built
        return built
    }

private var _devices: ImageVector? = null

val Icons.Filled.Devices: ImageVector
    get() {
        _devices?.let { return it }
        val built = materialIcon(name = "Filled.Devices") {
            materialPath {
                moveTo(4.0f, 6.0f)
                horizontalLineToRelative(18.0f)
                lineTo(22.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(11.0f)
                lineTo(0.0f, 17.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-3.0f)
                lineTo(4.0f, 17.0f)
                lineTo(4.0f, 6.0f)
                close()
                moveTo(23.0f, 8.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(24.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(22.0f, 17.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(7.0f)
                close()
            }
        }
        _devices = built
        return built
    }

private var _displaySettings: ImageVector? = null

val Icons.Filled.DisplaySettings: ImageVector
    get() {
        _displaySettings?.let { return it }
        val built = materialIcon(name = "Filled.DisplaySettings") {
            materialPath {
                moveTo(20.0f, 3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.89f, 3.0f, 2.0f, 3.89f, 2.0f, 5.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(22.0f, 3.89f, 21.1f, 3.0f, 20.0f, 3.0f)
                close()
                moveTo(20.0f, 17.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(17.0f)
                close()
            }
            materialPath {
                moveTo(6.0f, 8.25f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(1.5f)
                horizontalLineToRelative(-8.0f)
                close()
            }
            materialPath {
                moveTo(16.5f, 9.75f)
                lineToRelative(1.5f, 0.0f)
                lineToRelative(0.0f, -1.5f)
                lineToRelative(-1.5f, 0.0f)
                lineToRelative(0.0f, -1.25f)
                lineToRelative(-1.5f, 0.0f)
                lineToRelative(0.0f, 4.0f)
                lineToRelative(1.5f, 0.0f)
                close()
            }
            materialPath {
                moveTo(10.0f, 12.25f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(1.5f)
                horizontalLineToRelative(-8.0f)
                close()
            }
            materialPath {
                moveTo(7.5f, 15.0f)
                lineToRelative(1.5f, 0.0f)
                lineToRelative(0.0f, -4.0f)
                lineToRelative(-1.5f, 0.0f)
                lineToRelative(0.0f, 1.25f)
                lineToRelative(-1.5f, 0.0f)
                lineToRelative(0.0f, 1.5f)
                lineToRelative(1.5f, 0.0f)
                close()
            }
        }
        _displaySettings = built
        return built
    }

private var _download: ImageVector? = null

val Icons.Filled.Download: ImageVector
    get() {
        _download?.let { return it }
        val built = materialIcon(name = "Filled.Download") {
            materialPath {
                moveTo(5.0f, 20.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineTo(5.0f)
                lineToRelative(7.0f, 7.0f)
                lineTo(19.0f, 9.0f)
                close()
            }
        }
        _download = built
        return built
    }

private var _errorOutline: ImageVector? = null

val Icons.Filled.ErrorOutline: ImageVector
    get() {
        _errorOutline?.let { return it }
        val built = materialIcon(name = "Filled.ErrorOutline") {
            materialPath {
                moveTo(11.0f, 15.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
        }
        _errorOutline = built
        return built
    }

private var _fileDownload: ImageVector? = null

val Icons.Filled.FileDownload: ImageVector
    get() {
        _fileDownload?.let { return it }
        val built = materialIcon(name = "Filled.FileDownload") {
            materialPath {
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineTo(5.0f)
                lineToRelative(7.0f, 7.0f)
                lineToRelative(7.0f, -7.0f)
                close()
                moveTo(5.0f, 18.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                close()
            }
        }
        _fileDownload = built
        return built
    }

private var _folder: ImageVector? = null

val Icons.Filled.Folder: ImageVector
    get() {
        _folder?.let { return it }
        val built = materialIcon(name = "Filled.Folder") {
            materialPath {
                moveTo(10.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-8.0f)
                lineToRelative(-2.0f, -2.0f)
                close()
            }
        }
        _folder = built
        return built
    }

private var _folderOpen: ImageVector? = null

val Icons.Filled.FolderOpen: ImageVector
    get() {
        _folderOpen?.let { return it }
        val built = materialIcon(name = "Filled.FolderOpen") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-8.0f)
                lineToRelative(-2.0f, -2.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 18.0f)
                lineTo(4.0f, 18.0f)
                lineTo(4.0f, 8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(10.0f)
                close()
            }
        }
        _folderOpen = built
        return built
    }

private var _folderSpecial: ImageVector? = null

val Icons.Filled.FolderSpecial: ImageVector
    get() {
        _folderSpecial?.let { return it }
        val built = materialIcon(name = "Filled.FolderSpecial") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-8.0f)
                lineToRelative(-2.0f, -2.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.94f, 17.0f)
                lineTo(15.0f, 15.28f)
                lineTo(12.06f, 17.0f)
                lineToRelative(0.78f, -3.33f)
                lineToRelative(-2.59f, -2.24f)
                lineToRelative(3.41f, -0.29f)
                lineTo(15.0f, 8.0f)
                lineToRelative(1.34f, 3.14f)
                lineToRelative(3.41f, 0.29f)
                lineToRelative(-2.59f, 2.24f)
                lineToRelative(0.78f, 3.33f)
                close()
            }
        }
        _folderSpecial = built
        return built
    }

private var _formatSize: ImageVector? = null

val Icons.Filled.FormatSize: ImageVector
    get() {
        _formatSize?.let { return it }
        val built = materialIcon(name = "Filled.FormatSize") {
            materialPath {
                moveTo(9.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(3.0f)
                lineTo(17.0f, 7.0f)
                horizontalLineToRelative(5.0f)
                lineTo(22.0f, 4.0f)
                lineTo(9.0f, 4.0f)
                close()
                moveTo(3.0f, 12.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(3.0f)
                lineTo(12.0f, 9.0f)
                lineTo(3.0f, 9.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        _formatSize = built
        return built
    }

private var _gamepad: ImageVector? = null

val Icons.Filled.Gamepad: ImageVector
    get() {
        _gamepad?.let { return it }
        val built = materialIcon(name = "Filled.Gamepad") {
            materialPath {
                moveTo(15.0f, 7.5f)
                verticalLineTo(2.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(5.5f)
                lineToRelative(3.0f, 3.0f)
                lineToRelative(3.0f, -3.0f)
                close()
                moveTo(7.5f, 9.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(5.5f)
                lineToRelative(3.0f, -3.0f)
                lineToRelative(-3.0f, -3.0f)
                close()
                moveTo(9.0f, 16.5f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-5.5f)
                lineToRelative(-3.0f, -3.0f)
                lineToRelative(-3.0f, 3.0f)
                close()
                moveTo(16.5f, 9.0f)
                lineToRelative(-3.0f, 3.0f)
                lineToRelative(3.0f, 3.0f)
                horizontalLineTo(22.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(-5.5f)
                close()
            }
        }
        _gamepad = built
        return built
    }

private var _getApp: ImageVector? = null

val Icons.Filled.GetApp: ImageVector
    get() {
        _getApp?.let { return it }
        val built = materialIcon(name = "Filled.GetApp") {
            materialPath {
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineTo(5.0f)
                lineToRelative(7.0f, 7.0f)
                lineToRelative(7.0f, -7.0f)
                close()
                moveTo(5.0f, 18.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                close()
            }
        }
        _getApp = built
        return built
    }

private var _gridView: ImageVector? = null

val Icons.Filled.GridView: ImageVector
    get() {
        _gridView?.let { return it }
        val built = materialIcon(name = "Filled.GridView") {
            materialPath(pathFillType = PathFillType.EvenOdd) {
                moveTo(3.0f, 3.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                lineTo(11.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                close()
                moveTo(9.0f, 9.0f)
                lineTo(5.0f, 9.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(3.0f, 13.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                lineTo(3.0f, 13.0f)
                close()
                moveTo(9.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(13.0f, 3.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                lineTo(21.0f, 3.0f)
                horizontalLineToRelative(-8.0f)
                close()
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(15.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(13.0f, 13.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(-8.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        _gridView = built
        return built
    }

private var _history: ImageVector? = null

val Icons.Filled.History: ImageVector
    get() {
        _history?.let { return it }
        val built = materialIcon(name = "Filled.History") {
            materialPath {
                moveTo(13.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                lineTo(1.0f, 12.0f)
                lineToRelative(3.89f, 3.89f)
                lineToRelative(0.07f, 0.14f)
                lineTo(9.0f, 12.0f)
                lineTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
                curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 8.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(4.28f, 2.54f)
                lineToRelative(0.72f, -1.21f)
                lineToRelative(-3.5f, -2.08f)
                lineTo(13.5f, 8.0f)
                lineTo(12.0f, 8.0f)
                close()
            }
        }
        _history = built
        return built
    }

private var _image: ImageVector? = null

val Icons.Filled.Image: ImageVector
    get() {
        _image?.let { return it }
        val built = materialIcon(name = "Filled.Image") {
            materialPath {
                moveTo(21.0f, 19.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
                moveTo(8.5f, 13.5f)
                lineToRelative(2.5f, 3.01f)
                lineTo(14.5f, 12.0f)
                lineToRelative(4.5f, 6.0f)
                horizontalLineTo(5.0f)
                lineToRelative(3.5f, -4.5f)
                close()
            }
        }
        _image = built
        return built
    }

private var _language: ImageVector? = null

val Icons.Filled.Language: ImageVector
    get() {
        _language?.let { return it }
        val built = materialIcon(name = "Filled.Language") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(18.92f, 8.0f)
                horizontalLineToRelative(-2.95f)
                curveToRelative(-0.32f, -1.25f, -0.78f, -2.45f, -1.38f, -3.56f)
                curveToRelative(1.84f, 0.63f, 3.37f, 1.91f, 4.33f, 3.56f)
                close()
                moveTo(12.0f, 4.04f)
                curveToRelative(0.83f, 1.2f, 1.48f, 2.53f, 1.91f, 3.96f)
                horizontalLineToRelative(-3.82f)
                curveToRelative(0.43f, -1.43f, 1.08f, -2.76f, 1.91f, -3.96f)
                close()
                moveTo(4.26f, 14.0f)
                curveTo(4.1f, 13.36f, 4.0f, 12.69f, 4.0f, 12.0f)
                reflectiveCurveToRelative(0.1f, -1.36f, 0.26f, -2.0f)
                horizontalLineToRelative(3.38f)
                curveToRelative(-0.08f, 0.66f, -0.14f, 1.32f, -0.14f, 2.0f)
                curveToRelative(0.0f, 0.68f, 0.06f, 1.34f, 0.14f, 2.0f)
                lineTo(4.26f, 14.0f)
                close()
                moveTo(5.08f, 16.0f)
                horizontalLineToRelative(2.95f)
                curveToRelative(0.32f, 1.25f, 0.78f, 2.45f, 1.38f, 3.56f)
                curveToRelative(-1.84f, -0.63f, -3.37f, -1.9f, -4.33f, -3.56f)
                close()
                moveTo(8.03f, 8.0f)
                lineTo(5.08f, 8.0f)
                curveToRelative(0.96f, -1.66f, 2.49f, -2.93f, 4.33f, -3.56f)
                curveTo(8.81f, 5.55f, 8.35f, 6.75f, 8.03f, 8.0f)
                close()
                moveTo(12.0f, 19.96f)
                curveToRelative(-0.83f, -1.2f, -1.48f, -2.53f, -1.91f, -3.96f)
                horizontalLineToRelative(3.82f)
                curveToRelative(-0.43f, 1.43f, -1.08f, 2.76f, -1.91f, 3.96f)
                close()
                moveTo(14.34f, 14.0f)
                lineTo(9.66f, 14.0f)
                curveToRelative(-0.09f, -0.66f, -0.16f, -1.32f, -0.16f, -2.0f)
                curveToRelative(0.0f, -0.68f, 0.07f, -1.35f, 0.16f, -2.0f)
                horizontalLineToRelative(4.68f)
                curveToRelative(0.09f, 0.65f, 0.16f, 1.32f, 0.16f, 2.0f)
                curveToRelative(0.0f, 0.68f, -0.07f, 1.34f, -0.16f, 2.0f)
                close()
                moveTo(14.59f, 19.56f)
                curveToRelative(0.6f, -1.11f, 1.06f, -2.31f, 1.38f, -3.56f)
                horizontalLineToRelative(2.95f)
                curveToRelative(-0.96f, 1.65f, -2.49f, 2.93f, -4.33f, 3.56f)
                close()
                moveTo(16.36f, 14.0f)
                curveToRelative(0.08f, -0.66f, 0.14f, -1.32f, 0.14f, -2.0f)
                curveToRelative(0.0f, -0.68f, -0.06f, -1.34f, -0.14f, -2.0f)
                horizontalLineToRelative(3.38f)
                curveToRelative(0.16f, 0.64f, 0.26f, 1.31f, 0.26f, 2.0f)
                reflectiveCurveToRelative(-0.1f, 1.36f, -0.26f, 2.0f)
                horizontalLineToRelative(-3.38f)
                close()
            }
        }
        _language = built
        return built
    }

private var _liveTv: ImageVector? = null

val Icons.Filled.LiveTv: ImageVector
    get() {
        _liveTv?.let { return it }
        val built = materialIcon(name = "Filled.LiveTv") {
            materialPath {
                moveTo(21.0f, 6.0f)
                horizontalLineToRelative(-7.59f)
                lineToRelative(3.29f, -3.29f)
                lineTo(16.0f, 2.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(-4.0f, -4.0f)
                lineToRelative(-0.71f, 0.71f)
                lineTo(10.59f, 6.0f)
                lineTo(3.0f, 6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.89f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(23.0f, 8.0f)
                curveToRelative(0.0f, -1.11f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(21.0f, 20.0f)
                lineTo(3.0f, 20.0f)
                lineTo(3.0f, 8.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(12.0f)
                close()
                moveTo(9.0f, 10.0f)
                verticalLineToRelative(8.0f)
                lineToRelative(7.0f, -4.0f)
                close()
            }
        }
        _liveTv = built
        return built
    }

private var _lockOpen: ImageVector? = null

val Icons.Filled.LockOpen: ImageVector
    get() {
        _lockOpen?.let { return it }
        val built = materialIcon(name = "Filled.LockOpen") {
            materialPath {
                moveTo(12.0f, 17.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(18.0f, 8.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(17.0f, 6.0f)
                curveToRelative(0.0f, -2.76f, -2.24f, -5.0f, -5.0f, -5.0f)
                reflectiveCurveTo(7.0f, 3.24f, 7.0f, 6.0f)
                horizontalLineToRelative(1.9f)
                curveToRelative(0.0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                curveToRelative(1.71f, 0.0f, 3.1f, 1.39f, 3.1f, 3.1f)
                verticalLineToRelative(2.0f)
                lineTo(6.0f, 8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 10.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 20.0f)
                lineTo(6.0f, 20.0f)
                lineTo(6.0f, 10.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(10.0f)
                close()
            }
        }
        _lockOpen = built
        return built
    }

private var _memory: ImageVector? = null

val Icons.Filled.Memory: ImageVector
    get() {
        _memory?.let { return it }
        val built = materialIcon(name = "Filled.Memory") {
            materialPath {
                moveTo(15.0f, 9.0f)
                lineTo(9.0f, 9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(6.0f)
                lineTo(15.0f, 9.0f)
                close()
                moveTo(13.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(21.0f, 11.0f)
                lineTo(21.0f, 9.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(19.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(15.0f, 3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 3.0f)
                lineTo(9.0f, 3.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                lineTo(3.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(3.0f, 13.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(17.0f, 17.0f)
                lineTo(7.0f, 17.0f)
                lineTo(7.0f, 7.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(10.0f)
                close()
            }
        }
        _memory = built
        return built
    }

private var _mic: ImageVector? = null

val Icons.Filled.Mic: ImageVector
    get() {
        _mic?.let { return it }
        val built = materialIcon(name = "Filled.Mic") {
            materialPath {
                moveTo(12.0f, 14.0f)
                curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                lineTo(15.0f, 5.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(17.3f, 11.0f)
                curveToRelative(0.0f, 3.0f, -2.54f, 5.1f, -5.3f, 5.1f)
                reflectiveCurveTo(6.7f, 14.0f, 6.7f, 11.0f)
                lineTo(5.0f, 11.0f)
                curveToRelative(0.0f, 3.41f, 2.72f, 6.23f, 6.0f, 6.72f)
                lineTo(11.0f, 21.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-3.28f)
                curveToRelative(3.28f, -0.48f, 6.0f, -3.3f, 6.0f, -6.72f)
                horizontalLineToRelative(-1.7f)
                close()
            }
        }
        _mic = built
        return built
    }

private var _micOff: ImageVector? = null

val Icons.Filled.MicOff: ImageVector
    get() {
        _micOff?.let { return it }
        val built = materialIcon(name = "Filled.MicOff") {
            materialPath {
                moveTo(19.0f, 11.0f)
                horizontalLineToRelative(-1.7f)
                curveToRelative(0.0f, 0.74f, -0.16f, 1.43f, -0.43f, 2.05f)
                lineToRelative(1.23f, 1.23f)
                curveToRelative(0.56f, -0.98f, 0.9f, -2.09f, 0.9f, -3.28f)
                close()
                moveTo(14.98f, 11.17f)
                curveToRelative(0.0f, -0.06f, 0.02f, -0.11f, 0.02f, -0.17f)
                lineTo(15.0f, 5.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                verticalLineToRelative(0.18f)
                lineToRelative(5.98f, 5.99f)
                close()
                moveTo(4.27f, 3.0f)
                lineTo(3.0f, 4.27f)
                lineToRelative(6.01f, 6.01f)
                lineTo(9.01f, 11.0f)
                curveToRelative(0.0f, 1.66f, 1.33f, 3.0f, 2.99f, 3.0f)
                curveToRelative(0.22f, 0.0f, 0.44f, -0.03f, 0.65f, -0.08f)
                lineToRelative(1.66f, 1.66f)
                curveToRelative(-0.71f, 0.33f, -1.5f, 0.52f, -2.31f, 0.52f)
                curveToRelative(-2.76f, 0.0f, -5.3f, -2.1f, -5.3f, -5.1f)
                lineTo(5.0f, 11.0f)
                curveToRelative(0.0f, 3.41f, 2.72f, 6.23f, 6.0f, 6.72f)
                lineTo(11.0f, 21.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-3.28f)
                curveToRelative(0.91f, -0.13f, 1.77f, -0.45f, 2.54f, -0.9f)
                lineTo(19.73f, 21.0f)
                lineTo(21.0f, 19.73f)
                lineTo(4.27f, 3.0f)
                close()
            }
        }
        _micOff = built
        return built
    }

private var _movie: ImageVector? = null

val Icons.Filled.Movie: ImageVector
    get() {
        _movie?.let { return it }
        val built = materialIcon(name = "Filled.Movie") {
            materialPath {
                moveTo(18.0f, 4.0f)
                lineToRelative(2.0f, 4.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-2.0f, -4.0f)
                horizontalLineToRelative(-2.0f)
                lineToRelative(2.0f, 4.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-2.0f, -4.0f)
                horizontalLineTo(8.0f)
                lineToRelative(2.0f, 4.0f)
                horizontalLineTo(7.0f)
                lineTo(5.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
        }
        _movie = built
        return built
    }

private var _musicNote: ImageVector? = null

val Icons.Filled.MusicNote: ImageVector
    get() {
        _musicNote?.let { return it }
        val built = materialIcon(name = "Filled.MusicNote") {
            materialPath {
                moveTo(12.0f, 3.0f)
                verticalLineToRelative(10.55f)
                curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2.0f, -0.55f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-6.0f)
                close()
            }
        }
        _musicNote = built
        return built
    }

private var _navigation: ImageVector? = null

val Icons.Filled.Navigation: ImageVector
    get() {
        _navigation?.let { return it }
        val built = materialIcon(name = "Filled.Navigation") {
            materialPath {
                moveTo(12.0f, 2.0f)
                lineTo(4.5f, 20.29f)
                lineToRelative(0.71f, 0.71f)
                lineTo(12.0f, 18.0f)
                lineToRelative(6.79f, 3.0f)
                lineToRelative(0.71f, -0.71f)
                close()
            }
        }
        _navigation = built
        return built
    }

private var _newspaper: ImageVector? = null

val Icons.Filled.Newspaper: ImageVector
    get() {
        _newspaper?.let { return it }
        val built = materialIcon(name = "Filled.Newspaper") {
            materialPath {
                moveTo(22.0f, 3.0f)
                lineToRelative(-1.67f, 1.67f)
                lineTo(18.67f, 3.0f)
                lineTo(17.0f, 4.67f)
                lineTo(15.33f, 3.0f)
                lineToRelative(-1.66f, 1.67f)
                lineTo(12.0f, 3.0f)
                lineToRelative(-1.67f, 1.67f)
                lineTo(8.67f, 3.0f)
                lineTo(7.0f, 4.67f)
                lineTo(5.33f, 3.0f)
                lineTo(3.67f, 4.67f)
                lineTo(2.0f, 3.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                lineToRelative(16.0f, 0.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(3.0f)
                close()
                moveTo(11.0f, 19.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(20.0f, 19.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(20.0f, 15.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(20.0f, 11.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(11.0f)
                close()
            }
        }
        _newspaper = built
        return built
    }

private var _opacity: ImageVector? = null

val Icons.Filled.Opacity: ImageVector
    get() {
        _opacity?.let { return it }
        val built = materialIcon(name = "Filled.Opacity") {
            materialPath {
                moveTo(17.66f, 8.0f)
                lineTo(12.0f, 2.35f)
                lineTo(6.34f, 8.0f)
                curveTo(4.78f, 9.56f, 4.0f, 11.64f, 4.0f, 13.64f)
                reflectiveCurveToRelative(0.78f, 4.11f, 2.34f, 5.67f)
                reflectiveCurveToRelative(3.61f, 2.35f, 5.66f, 2.35f)
                reflectiveCurveToRelative(4.1f, -0.79f, 5.66f, -2.35f)
                reflectiveCurveTo(20.0f, 15.64f, 20.0f, 13.64f)
                reflectiveCurveTo(19.22f, 9.56f, 17.66f, 8.0f)
                close()
                moveTo(6.0f, 14.0f)
                curveToRelative(0.01f, -2.0f, 0.62f, -3.27f, 1.76f, -4.4f)
                lineTo(12.0f, 5.27f)
                lineToRelative(4.24f, 4.38f)
                curveTo(17.38f, 10.77f, 17.99f, 12.0f, 18.0f, 14.0f)
                horizontalLineTo(6.0f)
                close()
            }
        }
        _opacity = built
        return built
    }

private var _openInFull: ImageVector? = null

val Icons.Filled.OpenInFull: ImageVector
    get() {
        _openInFull?.let { return it }
        val built = materialIcon(name = "Filled.OpenInFull") {
            materialPath {
                moveTo(21.0f, 11.0f)
                lineToRelative(0.0f, -8.0f)
                lineToRelative(-8.0f, 0.0f)
                lineToRelative(3.29f, 3.29f)
                lineToRelative(-10.0f, 10.0f)
                lineToRelative(-3.29f, -3.29f)
                lineToRelative(0.0f, 8.0f)
                lineToRelative(8.0f, 0.0f)
                lineToRelative(-3.29f, -3.29f)
                lineToRelative(10.0f, -10.0f)
                close()
            }
        }
        _openInFull = built
        return built
    }

private var _openWith: ImageVector? = null

val Icons.Filled.OpenWith: ImageVector
    get() {
        _openWith?.let { return it }
        val built = materialIcon(name = "Filled.OpenWith") {
            materialPath {
                moveTo(10.0f, 9.0f)
                horizontalLineToRelative(4.0f)
                lineTo(14.0f, 6.0f)
                horizontalLineToRelative(3.0f)
                lineToRelative(-5.0f, -5.0f)
                lineToRelative(-5.0f, 5.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(3.0f)
                close()
                moveTo(9.0f, 10.0f)
                lineTo(6.0f, 10.0f)
                lineTo(6.0f, 7.0f)
                lineToRelative(-5.0f, 5.0f)
                lineToRelative(5.0f, 5.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-4.0f)
                close()
                moveTo(23.0f, 12.0f)
                lineToRelative(-5.0f, -5.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(3.0f)
                lineToRelative(5.0f, -5.0f)
                close()
                moveTo(14.0f, 15.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(3.0f)
                lineTo(7.0f, 18.0f)
                lineToRelative(5.0f, 5.0f)
                lineToRelative(5.0f, -5.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(-3.0f)
                close()
            }
        }
        _openWith = built
        return built
    }

private var _pause: ImageVector? = null

val Icons.Filled.Pause: ImageVector
    get() {
        _pause?.let { return it }
        val built = materialIcon(name = "Filled.Pause") {
            materialPath {
                moveTo(6.0f, 19.0f)
                horizontalLineToRelative(4.0f)
                lineTo(10.0f, 5.0f)
                lineTo(6.0f, 5.0f)
                verticalLineToRelative(14.0f)
                close()
                moveTo(14.0f, 5.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(4.0f)
                lineTo(18.0f, 5.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
        }
        _pause = built
        return built
    }

private var _photoLibrary: ImageVector? = null

val Icons.Filled.PhotoLibrary: ImageVector
    get() {
        _photoLibrary?.let { return it }
        val built = materialIcon(name = "Filled.PhotoLibrary") {
            materialPath {
                moveTo(22.0f, 16.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(8.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
                moveTo(11.0f, 12.0f)
                lineToRelative(2.03f, 2.71f)
                lineTo(16.0f, 11.0f)
                lineToRelative(4.0f, 5.0f)
                lineTo(8.0f, 16.0f)
                lineToRelative(3.0f, -4.0f)
                close()
                moveTo(2.0f, 6.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                lineTo(4.0f, 20.0f)
                lineTo(4.0f, 6.0f)
                lineTo(2.0f, 6.0f)
                close()
            }
        }
        _photoLibrary = built
        return built
    }

private var _pictureInPictureAlt: ImageVector? = null

val Icons.Filled.PictureInPictureAlt: ImageVector
    get() {
        _pictureInPictureAlt?.let { return it }
        val built = materialIcon(name = "Filled.PictureInPictureAlt") {
            materialPath {
                moveTo(19.0f, 11.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-6.0f)
                close()
                moveTo(23.0f, 19.0f)
                lineTo(23.0f, 4.98f)
                curveTo(23.0f, 3.88f, 22.1f, 3.0f, 21.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.88f, -2.0f, 1.98f)
                lineTo(1.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
                moveTo(21.0f, 19.02f)
                lineTo(3.0f, 19.02f)
                lineTo(3.0f, 4.97f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(14.05f)
                close()
            }
        }
        _pictureInPictureAlt = built
        return built
    }

private var _playlistAddCheck: ImageVector? = null

val Icons.Filled.PlaylistAddCheck: ImageVector
    get() {
        _playlistAddCheck?.let { return it }
        val built = materialIcon(name = "Filled.PlaylistAddCheck") {
            materialPath {
                moveTo(3.0f, 10.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-11.0f)
                close()
            }
            materialPath {
                moveTo(3.0f, 6.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-11.0f)
                close()
            }
            materialPath {
                moveTo(3.0f, 14.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-7.0f)
                close()
            }
            materialPath {
                moveTo(20.59f, 11.93f)
                lineToRelative(-4.25f, 4.24f)
                lineToRelative(-2.12f, -2.12f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(3.53f, 3.54f)
                lineToRelative(5.66f, -5.66f)
                close()
            }
        }
        _playlistAddCheck = built
        return built
    }

private var _public: ImageVector? = null

val Icons.Filled.Public: ImageVector
    get() {
        _public?.let { return it }
        val built = materialIcon(name = "Filled.Public") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(11.0f, 19.93f)
                curveToRelative(-3.95f, -0.49f, -7.0f, -3.85f, -7.0f, -7.93f)
                curveToRelative(0.0f, -0.62f, 0.08f, -1.21f, 0.21f, -1.79f)
                lineTo(9.0f, 15.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                verticalLineToRelative(1.93f)
                close()
                moveTo(17.9f, 17.39f)
                curveToRelative(-0.26f, -0.81f, -1.0f, -1.39f, -1.9f, -1.39f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(8.0f, 12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-0.41f)
                curveToRelative(2.93f, 1.19f, 5.0f, 4.06f, 5.0f, 7.41f)
                curveToRelative(0.0f, 2.08f, -0.8f, 3.97f, -2.1f, 5.39f)
                close()
            }
        }
        _public = built
        return built
    }

private var _recordVoiceOver: ImageVector? = null

val Icons.Filled.RecordVoiceOver: ImageVector
    get() {
        _recordVoiceOver?.let { return it }
        val built = materialIcon(name = "Filled.RecordVoiceOver") {
            materialPath {
                moveTo(9.0f, 9.0f)
                moveToRelative(-4.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
            }
            materialPath {
                moveTo(9.0f, 15.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
                close()
                moveTo(16.76f, 5.36f)
                lineToRelative(-1.68f, 1.69f)
                curveToRelative(0.84f, 1.18f, 0.84f, 2.71f, 0.0f, 3.89f)
                lineToRelative(1.68f, 1.69f)
                curveToRelative(2.02f, -2.02f, 2.02f, -5.07f, 0.0f, -7.27f)
                close()
                moveTo(20.07f, 2.0f)
                lineToRelative(-1.63f, 1.63f)
                curveToRelative(2.77f, 3.02f, 2.77f, 7.56f, 0.0f, 10.74f)
                lineTo(20.07f, 16.0f)
                curveToRelative(3.9f, -3.89f, 3.91f, -9.95f, 0.0f, -14.0f)
                close()
            }
        }
        _recordVoiceOver = built
        return built
    }

private var _restore: ImageVector? = null

val Icons.Filled.Restore: ImageVector
    get() {
        _restore?.let { return it }
        val built = materialIcon(name = "Filled.Restore") {
            materialPath {
                moveTo(13.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                lineTo(1.0f, 12.0f)
                lineToRelative(3.89f, 3.89f)
                lineToRelative(0.07f, 0.14f)
                lineTo(9.0f, 12.0f)
                lineTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
                curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 8.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(4.28f, 2.54f)
                lineToRelative(0.72f, -1.21f)
                lineToRelative(-3.5f, -2.08f)
                lineTo(13.5f, 8.0f)
                lineTo(12.0f, 8.0f)
                close()
            }
        }
        _restore = built
        return built
    }

private var _save: ImageVector? = null

val Icons.Filled.Save: ImageVector
    get() {
        _save?.let { return it }
        val built = materialIcon(name = "Filled.Save") {
            materialPath {
                moveTo(17.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 7.0f)
                lineToRelative(-4.0f, -4.0f)
                close()
                moveTo(12.0f, 19.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                close()
                moveTo(15.0f, 9.0f)
                lineTo(5.0f, 9.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        _save = built
        return built
    }

private var _schedule: ImageVector? = null

val Icons.Filled.Schedule: ImageVector
    get() {
        _schedule?.let { return it }
        val built = materialIcon(name = "Filled.Schedule") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.5f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }
        _schedule = built
        return built
    }

private var _security: ImageVector? = null

val Icons.Filled.Security: ImageVector
    get() {
        _security?.let { return it }
        val built = materialIcon(name = "Filled.Security") {
            materialPath {
                moveTo(12.0f, 1.0f)
                lineTo(3.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 5.55f, 3.84f, 10.74f, 9.0f, 12.0f)
                curveToRelative(5.16f, -1.26f, 9.0f, -6.45f, 9.0f, -12.0f)
                lineTo(21.0f, 5.0f)
                lineToRelative(-9.0f, -4.0f)
                close()
                moveTo(12.0f, 11.99f)
                horizontalLineToRelative(7.0f)
                curveToRelative(-0.53f, 4.12f, -3.28f, 7.79f, -7.0f, 8.94f)
                lineTo(12.0f, 12.0f)
                lineTo(5.0f, 12.0f)
                lineTo(5.0f, 6.3f)
                lineToRelative(7.0f, -3.11f)
                verticalLineToRelative(8.8f)
                close()
            }
        }
        _security = built
        return built
    }

private var _sensors: ImageVector? = null

val Icons.Filled.Sensors: ImageVector
    get() {
        _sensors?.let { return it }
        val built = materialIcon(name = "Filled.Sensors") {
            materialPath {
                moveTo(7.76f, 16.24f)
                curveTo(6.67f, 15.16f, 6.0f, 13.66f, 6.0f, 12.0f)
                reflectiveCurveToRelative(0.67f, -3.16f, 1.76f, -4.24f)
                lineToRelative(1.42f, 1.42f)
                curveTo(8.45f, 9.9f, 8.0f, 10.9f, 8.0f, 12.0f)
                curveToRelative(0.0f, 1.1f, 0.45f, 2.1f, 1.17f, 2.83f)
                lineTo(7.76f, 16.24f)
                close()
                moveTo(16.24f, 16.24f)
                curveTo(17.33f, 15.16f, 18.0f, 13.66f, 18.0f, 12.0f)
                reflectiveCurveToRelative(-0.67f, -3.16f, -1.76f, -4.24f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(15.55f, 9.9f, 16.0f, 10.9f, 16.0f, 12.0f)
                curveToRelative(0.0f, 1.1f, -0.45f, 2.1f, -1.17f, 2.83f)
                lineTo(16.24f, 16.24f)
                close()
                moveTo(12.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveTo(13.1f, 10.0f, 12.0f, 10.0f)
                close()
                moveTo(20.0f, 12.0f)
                curveToRelative(0.0f, 2.21f, -0.9f, 4.21f, -2.35f, 5.65f)
                lineToRelative(1.42f, 1.42f)
                curveTo(20.88f, 17.26f, 22.0f, 14.76f, 22.0f, 12.0f)
                reflectiveCurveToRelative(-1.12f, -5.26f, -2.93f, -7.07f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(19.1f, 7.79f, 20.0f, 9.79f, 20.0f, 12.0f)
                close()
                moveTo(6.35f, 6.35f)
                lineTo(4.93f, 4.93f)
                curveTo(3.12f, 6.74f, 2.0f, 9.24f, 2.0f, 12.0f)
                reflectiveCurveToRelative(1.12f, 5.26f, 2.93f, 7.07f)
                lineToRelative(1.42f, -1.42f)
                curveTo(4.9f, 16.21f, 4.0f, 14.21f, 4.0f, 12.0f)
                reflectiveCurveTo(4.9f, 7.79f, 6.35f, 6.35f)
                close()
            }
        }
        _sensors = built
        return built
    }

private var _settingsEthernet: ImageVector? = null

val Icons.Filled.SettingsEthernet: ImageVector
    get() {
        _settingsEthernet?.let { return it }
        val built = materialIcon(name = "Filled.SettingsEthernet") {
            materialPath {
                moveTo(7.77f, 6.76f)
                lineTo(6.23f, 5.48f)
                lineTo(0.82f, 12.0f)
                lineToRelative(5.41f, 6.52f)
                lineToRelative(1.54f, -1.28f)
                lineTo(3.42f, 12.0f)
                lineToRelative(4.35f, -5.24f)
                close()
                moveTo(7.0f, 13.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                lineTo(7.0f, 11.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(17.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(11.0f, 13.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(17.77f, 5.48f)
                lineToRelative(-1.54f, 1.28f)
                lineTo(20.58f, 12.0f)
                lineToRelative(-4.35f, 5.24f)
                lineToRelative(1.54f, 1.28f)
                lineTo(23.18f, 12.0f)
                lineToRelative(-5.41f, -6.52f)
                close()
            }
        }
        _settingsEthernet = built
        return built
    }

private var _settingsRemote: ImageVector? = null

val Icons.Filled.SettingsRemote: ImageVector
    get() {
        _settingsRemote?.let { return it }
        val built = materialIcon(name = "Filled.SettingsRemote") {
            materialPath {
                moveTo(15.0f, 9.0f)
                lineTo(9.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(16.0f, 10.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(12.0f, 15.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(7.05f, 6.05f)
                lineToRelative(1.41f, 1.41f)
                curveTo(9.37f, 6.56f, 10.62f, 6.0f, 12.0f, 6.0f)
                reflectiveCurveToRelative(2.63f, 0.56f, 3.54f, 1.46f)
                lineToRelative(1.41f, -1.41f)
                curveTo(15.68f, 4.78f, 13.93f, 4.0f, 12.0f, 4.0f)
                reflectiveCurveToRelative(-3.68f, 0.78f, -4.95f, 2.05f)
                close()
                moveTo(12.0f, 0.0f)
                curveTo(8.96f, 0.0f, 6.21f, 1.23f, 4.22f, 3.22f)
                lineToRelative(1.41f, 1.41f)
                curveTo(7.26f, 3.01f, 9.51f, 2.0f, 12.0f, 2.0f)
                reflectiveCurveToRelative(4.74f, 1.01f, 6.36f, 2.64f)
                lineToRelative(1.41f, -1.41f)
                curveTo(17.79f, 1.23f, 15.04f, 0.0f, 12.0f, 0.0f)
                close()
            }
        }
        _settingsRemote = built
        return built
    }

private var _skipNext: ImageVector? = null

val Icons.Filled.SkipNext: ImageVector
    get() {
        _skipNext?.let { return it }
        val built = materialIcon(name = "Filled.SkipNext") {
            materialPath {
                moveTo(6.0f, 18.0f)
                lineToRelative(8.5f, -6.0f)
                lineTo(6.0f, 6.0f)
                verticalLineToRelative(12.0f)
                close()
                moveTo(16.0f, 6.0f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(-2.0f)
                close()
            }
        }
        _skipNext = built
        return built
    }

private var _smartDisplay: ImageVector? = null

val Icons.Filled.SmartDisplay: ImageVector
    get() {
        _smartDisplay?.let { return it }
        val built = materialIcon(name = "Filled.SmartDisplay") {
            materialPath {
                moveTo(20.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(22.0f, 4.9f, 21.1f, 4.0f, 20.0f, 4.0f)
                close()
                moveTo(9.5f, 16.5f)
                verticalLineToRelative(-9.0f)
                lineToRelative(7.0f, 4.5f)
                lineTo(9.5f, 16.5f)
                close()
            }
        }
        _smartDisplay = built
        return built
    }

private var _speed: ImageVector? = null

val Icons.Filled.Speed: ImageVector
    get() {
        _speed?.let { return it }
        val built = materialIcon(name = "Filled.Speed") {
            materialPath {
                moveTo(20.38f, 8.57f)
                lineToRelative(-1.23f, 1.85f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -0.22f, 7.58f)
                lineTo(5.07f, 18.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 15.58f, 6.85f)
                lineToRelative(1.85f, -1.23f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 3.35f, 19.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 1.72f, 1.0f)
                horizontalLineToRelative(13.85f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 1.74f, -1.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, false, -0.27f, -10.44f)
                close()
                moveTo(10.59f, 15.41f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 2.83f, 0.0f)
                lineToRelative(5.66f, -8.49f)
                lineToRelative(-8.49f, 5.66f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 0.0f, 2.83f)
                close()
            }
        }
        _speed = built
        return built
    }

private var _sportsEsports: ImageVector? = null

val Icons.Filled.SportsEsports: ImageVector
    get() {
        _sportsEsports?.let { return it }
        val built = materialIcon(name = "Filled.SportsEsports") {
            materialPath {
                moveTo(21.58f, 16.09f)
                lineToRelative(-1.09f, -7.66f)
                curveTo(20.21f, 6.46f, 18.52f, 5.0f, 16.53f, 5.0f)
                horizontalLineTo(7.47f)
                curveTo(5.48f, 5.0f, 3.79f, 6.46f, 3.51f, 8.43f)
                lineToRelative(-1.09f, 7.66f)
                curveTo(2.2f, 17.63f, 3.39f, 19.0f, 4.94f, 19.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.68f, 0.0f, 1.32f, -0.27f, 1.8f, -0.75f)
                lineTo(9.0f, 16.0f)
                horizontalLineToRelative(6.0f)
                lineToRelative(2.25f, 2.25f)
                curveToRelative(0.48f, 0.48f, 1.13f, 0.75f, 1.8f, 0.75f)
                horizontalLineToRelative(0.0f)
                curveTo(20.61f, 19.0f, 21.8f, 17.63f, 21.58f, 16.09f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(16.0f, 9.55f, 15.55f, 10.0f, 15.0f, 10.0f)
                close()
                moveTo(17.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(18.0f, 12.55f, 17.55f, 13.0f, 17.0f, 13.0f)
                close()
            }
        }
        _sportsEsports = built
        return built
    }

private var _sportsSoccer: ImageVector? = null

val Icons.Filled.SportsSoccer: ImageVector
    get() {
        _sportsSoccer?.let { return it }
        val built = materialIcon(name = "Filled.SportsSoccer") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                curveToRelative(0.0f, 5.52f, 4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.0f, 5.3f)
                lineToRelative(1.35f, -0.95f)
                curveToRelative(1.82f, 0.56f, 3.37f, 1.76f, 4.38f, 3.34f)
                lineToRelative(-0.39f, 1.34f)
                lineToRelative(-1.35f, 0.46f)
                lineTo(13.0f, 6.7f)
                verticalLineTo(5.3f)
                close()
                moveTo(9.65f, 4.35f)
                lineTo(11.0f, 5.3f)
                verticalLineToRelative(1.4f)
                lineTo(7.01f, 9.49f)
                lineTo(5.66f, 9.03f)
                lineTo(5.27f, 7.69f)
                curveTo(6.28f, 6.12f, 7.83f, 4.92f, 9.65f, 4.35f)
                close()
                moveTo(7.08f, 17.11f)
                lineToRelative(-1.14f, 0.1f)
                curveTo(4.73f, 15.81f, 4.0f, 13.99f, 4.0f, 12.0f)
                curveToRelative(0.0f, -0.12f, 0.01f, -0.23f, 0.02f, -0.35f)
                lineToRelative(1.0f, -0.73f)
                lineTo(6.4f, 11.4f)
                lineToRelative(1.46f, 4.34f)
                lineTo(7.08f, 17.11f)
                close()
                moveTo(14.5f, 19.59f)
                curveTo(13.71f, 19.85f, 12.87f, 20.0f, 12.0f, 20.0f)
                reflectiveCurveToRelative(-1.71f, -0.15f, -2.5f, -0.41f)
                lineToRelative(-0.69f, -1.49f)
                lineTo(9.45f, 17.0f)
                horizontalLineToRelative(5.11f)
                lineToRelative(0.64f, 1.11f)
                lineTo(14.5f, 19.59f)
                close()
                moveTo(14.27f, 15.0f)
                horizontalLineTo(9.73f)
                lineToRelative(-1.35f, -4.02f)
                lineTo(12.0f, 8.44f)
                lineToRelative(3.63f, 2.54f)
                lineTo(14.27f, 15.0f)
                close()
                moveTo(18.06f, 17.21f)
                lineToRelative(-1.14f, -0.1f)
                lineToRelative(-0.79f, -1.37f)
                lineToRelative(1.46f, -4.34f)
                lineToRelative(1.39f, -0.47f)
                lineToRelative(1.0f, 0.73f)
                curveTo(19.99f, 11.77f, 20.0f, 11.88f, 20.0f, 12.0f)
                curveTo(20.0f, 13.99f, 19.27f, 15.81f, 18.06f, 17.21f)
                close()
            }
        }
        _sportsSoccer = built
        return built
    }

private var _starBorder: ImageVector? = null

val Icons.Filled.StarBorder: ImageVector
    get() {
        _starBorder?.let { return it }
        val built = materialIcon(name = "Filled.StarBorder") {
            materialPath {
                moveTo(22.0f, 9.24f)
                lineToRelative(-7.19f, -0.62f)
                lineTo(12.0f, 2.0f)
                lineTo(9.19f, 8.63f)
                lineTo(2.0f, 9.24f)
                lineToRelative(5.46f, 4.73f)
                lineTo(5.82f, 21.0f)
                lineTo(12.0f, 17.27f)
                lineTo(18.18f, 21.0f)
                lineToRelative(-1.63f, -7.03f)
                lineTo(22.0f, 9.24f)
                close()
                moveTo(12.0f, 15.4f)
                lineToRelative(-3.76f, 2.27f)
                lineToRelative(1.0f, -4.28f)
                lineToRelative(-3.32f, -2.88f)
                lineToRelative(4.38f, -0.38f)
                lineTo(12.0f, 6.1f)
                lineToRelative(1.71f, 4.04f)
                lineToRelative(4.38f, 0.38f)
                lineToRelative(-3.32f, 2.88f)
                lineToRelative(1.0f, 4.28f)
                lineTo(12.0f, 15.4f)
                close()
            }
        }
        _starBorder = built
        return built
    }

private var _storage: ImageVector? = null

val Icons.Filled.Storage: ImageVector
    get() {
        _storage?.let { return it }
        val built = materialIcon(name = "Filled.Storage") {
            materialPath {
                moveTo(2.0f, 20.0f)
                horizontalLineToRelative(20.0f)
                verticalLineToRelative(-4.0f)
                lineTo(2.0f, 16.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(4.0f, 17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(2.0f, 4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(20.0f)
                lineTo(22.0f, 4.0f)
                lineTo(2.0f, 4.0f)
                close()
                moveTo(6.0f, 7.0f)
                lineTo(4.0f, 7.0f)
                lineTo(4.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(2.0f, 14.0f)
                horizontalLineToRelative(20.0f)
                verticalLineToRelative(-4.0f)
                lineTo(2.0f, 10.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(4.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                close()
            }
        }
        _storage = built
        return built
    }

private var _systemUpdate: ImageVector? = null

val Icons.Filled.SystemUpdate: ImageVector
    get() {
        _systemUpdate?.let { return it }
        val built = materialIcon(name = "Filled.SystemUpdate") {
            materialPath {
                moveTo(17.0f, 1.01f)
                lineTo(7.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(19.0f, 3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -1.99f, -2.0f, -1.99f)
                close()
                moveTo(17.0f, 19.0f)
                lineTo(7.0f, 19.0f)
                lineTo(7.0f, 5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(14.0f)
                close()
                moveTo(16.0f, 13.0f)
                horizontalLineToRelative(-3.0f)
                lineTo(13.0f, 8.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(5.0f)
                lineTo(8.0f, 13.0f)
                lineToRelative(4.0f, 4.0f)
                lineToRelative(4.0f, -4.0f)
                close()
            }
        }
        _systemUpdate = built
        return built
    }

private var _timer: ImageVector? = null

val Icons.Filled.Timer: ImageVector
    get() {
        _timer?.let { return it }
        val built = materialIcon(name = "Filled.Timer") {
            materialPath {
                moveTo(9.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                close()
            }
            materialPath {
                moveTo(19.03f, 7.39f)
                lineToRelative(1.42f, -1.42f)
                curveToRelative(-0.43f, -0.51f, -0.9f, -0.99f, -1.41f, -1.41f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(16.07f, 4.74f, 14.12f, 4.0f, 12.0f, 4.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                curveToRelative(0.0f, 4.97f, 4.02f, 9.0f, 9.0f, 9.0f)
                reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
                curveTo(21.0f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
            }
        }
        _timer = built
        return built
    }

private var _touchApp: ImageVector? = null

val Icons.Filled.TouchApp: ImageVector
    get() {
        _touchApp?.let { return it }
        val built = materialIcon(name = "Filled.TouchApp") {
            materialPath {
                moveTo(9.0f, 11.24f)
                verticalLineTo(7.5f)
                curveTo(9.0f, 6.12f, 10.12f, 5.0f, 11.5f, 5.0f)
                reflectiveCurveTo(14.0f, 6.12f, 14.0f, 7.5f)
                verticalLineToRelative(3.74f)
                curveToRelative(1.21f, -0.81f, 2.0f, -2.18f, 2.0f, -3.74f)
                curveTo(16.0f, 5.01f, 13.99f, 3.0f, 11.5f, 3.0f)
                reflectiveCurveTo(7.0f, 5.01f, 7.0f, 7.5f)
                curveTo(7.0f, 9.06f, 7.79f, 10.43f, 9.0f, 11.24f)
                close()
                moveTo(18.84f, 15.87f)
                lineToRelative(-4.54f, -2.26f)
                curveToRelative(-0.17f, -0.07f, -0.35f, -0.11f, -0.54f, -0.11f)
                horizontalLineTo(13.0f)
                verticalLineToRelative(-6.0f)
                curveTo(13.0f, 6.67f, 12.33f, 6.0f, 11.5f, 6.0f)
                reflectiveCurveTo(10.0f, 6.67f, 10.0f, 7.5f)
                verticalLineToRelative(10.74f)
                curveToRelative(-3.6f, -0.76f, -3.54f, -0.75f, -3.67f, -0.75f)
                curveToRelative(-0.31f, 0.0f, -0.59f, 0.13f, -0.79f, 0.33f)
                lineToRelative(-0.79f, 0.8f)
                lineToRelative(4.94f, 4.94f)
                curveTo(9.96f, 23.83f, 10.34f, 24.0f, 10.75f, 24.0f)
                horizontalLineToRelative(6.79f)
                curveToRelative(0.75f, 0.0f, 1.33f, -0.55f, 1.44f, -1.28f)
                lineToRelative(0.75f, -5.27f)
                curveToRelative(0.01f, -0.07f, 0.02f, -0.14f, 0.02f, -0.2f)
                curveTo(19.75f, 16.63f, 19.37f, 16.09f, 18.84f, 15.87f)
                close()
            }
        }
        _touchApp = built
        return built
    }

private var _tune: ImageVector? = null

val Icons.Filled.Tune: ImageVector
    get() {
        _tune?.let { return it }
        val built = materialIcon(name = "Filled.Tune") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                lineTo(3.0f, 17.0f)
                close()
                moveTo(3.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                lineTo(13.0f, 5.0f)
                lineTo(3.0f, 5.0f)
                close()
                moveTo(13.0f, 21.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 9.0f)
                verticalLineToRelative(2.0f)
                lineTo(3.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                lineTo(9.0f, 9.0f)
                lineTo(7.0f, 9.0f)
                close()
                moveTo(21.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                lineTo(11.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                close()
                moveTo(15.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                lineTo(17.0f, 7.0f)
                horizontalLineToRelative(4.0f)
                lineTo(21.0f, 5.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(17.0f, 3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
        _tune = built
        return built
    }

private var _verticalAlignBottom: ImageVector? = null

val Icons.Filled.VerticalAlignBottom: ImageVector
    get() {
        _verticalAlignBottom?.let { return it }
        val built = materialIcon(name = "Filled.VerticalAlignBottom") {
            materialPath {
                moveTo(16.0f, 13.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(10.0f)
                horizontalLineTo(8.0f)
                lineToRelative(4.0f, 4.0f)
                lineToRelative(4.0f, -4.0f)
                close()
                moveTo(4.0f, 19.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(4.0f)
                close()
            }
        }
        _verticalAlignBottom = built
        return built
    }

private var _visibility: ImageVector? = null

val Icons.Filled.Visibility: ImageVector
    get() {
        _visibility?.let { return it }
        val built = materialIcon(name = "Filled.Visibility") {
            materialPath {
                moveTo(12.0f, 4.5f)
                curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        _visibility = built
        return built
    }

private var _visibilityOff: ImageVector? = null

val Icons.Filled.VisibilityOff: ImageVector
    get() {
        _visibilityOff?.let { return it }
        val built = materialIcon(name = "Filled.VisibilityOff") {
            materialPath {
                moveTo(12.0f, 7.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                curveToRelative(0.0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f)
                lineToRelative(2.92f, 2.92f)
                curveToRelative(1.51f, -1.26f, 2.7f, -2.89f, 3.43f, -4.75f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                curveToRelative(-1.4f, 0.0f, -2.74f, 0.25f, -3.98f, 0.7f)
                lineToRelative(2.16f, 2.16f)
                curveTo(10.74f, 7.13f, 11.35f, 7.0f, 12.0f, 7.0f)
                close()
                moveTo(2.0f, 4.27f)
                lineToRelative(2.28f, 2.28f)
                lineToRelative(0.46f, 0.46f)
                curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                curveToRelative(1.55f, 0.0f, 3.03f, -0.3f, 4.38f, -0.84f)
                lineToRelative(0.42f, 0.42f)
                lineTo(19.73f, 22.0f)
                lineTo(21.0f, 20.73f)
                lineTo(3.27f, 3.0f)
                lineTo(2.0f, 4.27f)
                close()
                moveTo(7.53f, 9.8f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.05f, 0.21f, -0.08f, 0.43f, -0.08f, 0.65f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                curveToRelative(0.22f, 0.0f, 0.44f, -0.03f, 0.65f, -0.08f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.67f, 0.33f, -1.41f, 0.53f, -2.2f, 0.53f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                curveToRelative(0.0f, -0.79f, 0.2f, -1.53f, 0.53f, -2.2f)
                close()
                moveTo(11.84f, 9.02f)
                lineToRelative(3.15f, 3.15f)
                lineToRelative(0.02f, -0.16f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                lineToRelative(-0.17f, 0.01f)
                close()
            }
        }
        _visibilityOff = built
        return built
    }

private var _volumeUp: ImageVector? = null

val Icons.Filled.VolumeUp: ImageVector
    get() {
        _volumeUp?.let { return it }
        val built = materialIcon(name = "Filled.VolumeUp") {
            materialPath {
                moveTo(3.0f, 9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(4.0f)
                lineToRelative(5.0f, 5.0f)
                lineTo(12.0f, 4.0f)
                lineTo(7.0f, 9.0f)
                lineTo(3.0f, 9.0f)
                close()
                moveTo(16.5f, 12.0f)
                curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                verticalLineToRelative(8.05f)
                curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
                close()
                moveTo(14.0f, 3.23f)
                verticalLineToRelative(2.06f)
                curveToRelative(2.89f, 0.86f, 5.0f, 3.54f, 5.0f, 6.71f)
                reflectiveCurveToRelative(-2.11f, 5.85f, -5.0f, 6.71f)
                verticalLineToRelative(2.06f)
                curveToRelative(4.01f, -0.91f, 7.0f, -4.49f, 7.0f, -8.77f)
                reflectiveCurveToRelative(-2.99f, -7.86f, -7.0f, -8.77f)
                close()
            }
        }
        _volumeUp = built
        return built
    }

private var _wallpaper: ImageVector? = null

val Icons.Filled.Wallpaper: ImageVector
    get() {
        _wallpaper?.let { return it }
        val built = materialIcon(name = "Filled.Wallpaper") {
            materialPath {
                moveTo(4.0f, 4.0f)
                horizontalLineToRelative(7.0f)
                lineTo(11.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(2.0f)
                lineTo(4.0f, 4.0f)
                close()
                moveTo(10.0f, 13.0f)
                lineToRelative(-4.0f, 5.0f)
                horizontalLineToRelative(12.0f)
                lineToRelative(-3.0f, -4.0f)
                lineToRelative(-2.03f, 2.71f)
                lineTo(10.0f, 13.0f)
                close()
                moveTo(17.0f, 8.5f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveTo(14.0f, 7.67f, 14.0f, 8.5f)
                reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveTo(17.0f, 9.33f, 17.0f, 8.5f)
                close()
                moveTo(20.0f, 2.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 20.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(7.0f)
                close()
                moveTo(4.0f, 13.0f)
                lineTo(2.0f, 13.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-2.0f)
                lineTo(4.0f, 20.0f)
                verticalLineToRelative(-7.0f)
                close()
            }
        }
        _wallpaper = built
        return built
    }

private var _waterDrop: ImageVector? = null

val Icons.Filled.WaterDrop: ImageVector
    get() {
        _waterDrop?.let { return it }
        val built = materialIcon(name = "Filled.WaterDrop") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveToRelative(-5.33f, 4.55f, -8.0f, 8.48f, -8.0f, 11.8f)
                curveToRelative(0.0f, 4.98f, 3.8f, 8.2f, 8.0f, 8.2f)
                reflectiveCurveToRelative(8.0f, -3.22f, 8.0f, -8.2f)
                curveTo(20.0f, 10.48f, 17.33f, 6.55f, 12.0f, 2.0f)
                close()
                moveTo(7.83f, 14.0f)
                curveToRelative(0.37f, 0.0f, 0.67f, 0.26f, 0.74f, 0.62f)
                curveToRelative(0.41f, 2.22f, 2.28f, 2.98f, 3.64f, 2.87f)
                curveToRelative(0.43f, -0.02f, 0.79f, 0.32f, 0.79f, 0.75f)
                curveToRelative(0.0f, 0.4f, -0.32f, 0.73f, -0.72f, 0.75f)
                curveToRelative(-2.13f, 0.13f, -4.62f, -1.09f, -5.19f, -4.12f)
                curveTo(7.01f, 14.42f, 7.37f, 14.0f, 7.83f, 14.0f)
                close()
            }
        }
        _waterDrop = built
        return built
    }

private var _wbSunny: ImageVector? = null

val Icons.Filled.WbSunny: ImageVector
    get() {
        _wbSunny?.let { return it }
        val built = materialIcon(name = "Filled.WbSunny") {
            materialPath {
                moveTo(6.76f, 4.84f)
                lineToRelative(-1.8f, -1.79f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(1.79f, 1.79f)
                lineToRelative(1.42f, -1.41f)
                close()
                moveTo(4.0f, 10.5f)
                lineTo(1.0f, 10.5f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(13.0f, 0.55f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 3.5f)
                horizontalLineToRelative(2.0f)
                lineTo(13.0f, 0.55f)
                close()
                moveTo(20.45f, 4.46f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-1.79f, 1.79f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(1.79f, -1.79f)
                close()
                moveTo(17.24f, 18.16f)
                lineToRelative(1.79f, 1.8f)
                lineToRelative(1.41f, -1.41f)
                lineToRelative(-1.8f, -1.79f)
                lineToRelative(-1.4f, 1.4f)
                close()
                moveTo(20.0f, 10.5f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(12.0f, 5.5f)
                curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
                reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
                reflectiveCurveToRelative(6.0f, -2.69f, 6.0f, -6.0f)
                reflectiveCurveToRelative(-2.69f, -6.0f, -6.0f, -6.0f)
                close()
                moveTo(11.0f, 22.45f)
                horizontalLineToRelative(2.0f)
                lineTo(13.0f, 19.5f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.95f)
                close()
                moveTo(3.55f, 18.54f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(1.79f, -1.8f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-1.79f, 1.8f)
                close()
            }
        }
        _wbSunny = built
        return built
    }

private var _wifi: ImageVector? = null

val Icons.Filled.Wifi: ImageVector
    get() {
        _wifi?.let { return it }
        val built = materialIcon(name = "Filled.Wifi") {
            materialPath {
                moveTo(1.0f, 9.0f)
                lineToRelative(2.0f, 2.0f)
                curveToRelative(4.97f, -4.97f, 13.03f, -4.97f, 18.0f, 0.0f)
                lineToRelative(2.0f, -2.0f)
                curveTo(16.93f, 2.93f, 7.08f, 2.93f, 1.0f, 9.0f)
                close()
                moveTo(9.0f, 17.0f)
                lineToRelative(3.0f, 3.0f)
                lineToRelative(3.0f, -3.0f)
                curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6.0f, 0.0f)
                close()
                moveTo(5.0f, 13.0f)
                lineToRelative(2.0f, 2.0f)
                curveToRelative(2.76f, -2.76f, 7.24f, -2.76f, 10.0f, 0.0f)
                lineToRelative(2.0f, -2.0f)
                curveTo(15.14f, 9.14f, 8.87f, 9.14f, 5.0f, 13.0f)
                close()
            }
        }
        _wifi = built
        return built
    }

private var _wifiOff: ImageVector? = null

val Icons.Filled.WifiOff: ImageVector
    get() {
        _wifiOff?.let { return it }
        val built = materialIcon(name = "Filled.WifiOff") {
            materialPath {
                moveTo(22.99f, 9.0f)
                curveTo(19.15f, 5.16f, 13.8f, 3.76f, 8.84f, 4.78f)
                lineToRelative(2.52f, 2.52f)
                curveToRelative(3.47f, -0.17f, 6.99f, 1.05f, 9.63f, 3.7f)
                lineToRelative(2.0f, -2.0f)
                close()
                moveTo(18.99f, 13.0f)
                curveToRelative(-1.29f, -1.29f, -2.84f, -2.13f, -4.49f, -2.56f)
                lineToRelative(3.53f, 3.53f)
                lineToRelative(0.96f, -0.97f)
                close()
                moveTo(2.0f, 3.05f)
                lineTo(5.07f, 6.1f)
                curveTo(3.6f, 6.82f, 2.22f, 7.78f, 1.0f, 9.0f)
                lineToRelative(1.99f, 2.0f)
                curveToRelative(1.24f, -1.24f, 2.67f, -2.16f, 4.2f, -2.77f)
                lineToRelative(2.24f, 2.24f)
                curveTo(7.81f, 10.89f, 6.27f, 11.73f, 5.0f, 13.0f)
                verticalLineToRelative(0.01f)
                lineTo(6.99f, 15.0f)
                curveToRelative(1.36f, -1.36f, 3.14f, -2.04f, 4.92f, -2.06f)
                lineTo(18.98f, 20.0f)
                lineToRelative(1.27f, -1.26f)
                lineTo(3.29f, 1.79f)
                lineTo(2.0f, 3.05f)
                close()
                moveTo(9.0f, 17.0f)
                lineToRelative(3.0f, 3.0f)
                lineToRelative(3.0f, -3.0f)
                curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6.0f, 0.0f)
                close()
            }
        }
        _wifiOff = built
        return built
    }
