package kr.ac.gachon.sw.safenoisecanceling

import android.util.Log
import androidx.wear.tiles.*
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.DimensionBuilders.expand
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

private const val RESOURCES_VERSION = "1"
private const val ID_IMAGE_ENABLE = "img_enable"
private const val ID_IMAGE_DISABLE = "img_disable"
private const val EVENT_ON_BTN = "event_on"
private const val EVENT_OFF_BTN = "event_off"
private const val BTN_SIZE = 150f

class SNCTileService: TileService() {
    private val key = "kr.ac.gachon.sw.safenoisecanceling.sncenable"
    private lateinit var dataClient: DataClient
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onTileRequest(requestParams: TileRequest) = serviceScope.future {
        update(requestParams.state?.lastClickableId == EVENT_ON_BTN)

        TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(
                                        layout(requestParams)
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    override fun onResourcesRequest(requestParams: ResourcesRequest) = serviceScope.future {
        ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                ID_IMAGE_DISABLE,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.wearable_disable)
                            .build()
                    )
                    .build()
            )
            .addIdToImageMapping(
                ID_IMAGE_ENABLE,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.wearable_enable)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        dataClient = Wearable.getDataClient(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun layout(requestParams: TileRequest) =
        LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .addContent(
                if (requestParams.state!!.lastClickableId == EVENT_ON_BTN) {
                    LayoutElementBuilders.Column.Builder()
                        .addContent(enableBtn())
                        .build()

                }
                else {
                    LayoutElementBuilders.Column.Builder()
                        .addContent(disableBtn())
                        .build()
                }
            )
            .build()

    private fun enableBtn() =
        LayoutElementBuilders.Image.Builder()
            .setWidth(dp(BTN_SIZE))
            .setHeight(dp(BTN_SIZE))
            .setResourceId(ID_IMAGE_ENABLE)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setId(EVENT_OFF_BTN)
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                    .build()
            ).build()

    private fun disableBtn() =
        LayoutElementBuilders.Image.Builder()
            .setWidth(dp(BTN_SIZE))
            .setHeight(dp(BTN_SIZE))
            .setResourceId(ID_IMAGE_DISABLE)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setId(EVENT_ON_BTN)
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                    .build()
            ).build()

    private fun update(enable: Boolean) {
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/snc_enable").run {
            dataMap.putBoolean(key, enable)
            asPutDataRequest()
        }
        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)
        putDataTask.addOnSuccessListener { Log.d("TEST", "OK with ${it}") }
    }
}