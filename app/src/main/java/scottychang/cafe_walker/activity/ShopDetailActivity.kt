package scottychang.cafe_walker.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import me.imid.swipebacklayout.lib.SwipeBackLayout
import me.imid.swipebacklayout.lib.Utils
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper
import scottychang.cafe_walker.R
import scottychang.cafe_walker.model.CoffeeShop
import scottychang.cafe_walker.viewmodel.CoffeeShopsViewModel
import java.net.URLDecoder




class ShopDetailActivity : AppCompatActivity(), SwipeBackActivityBase, OnMapReadyCallback {
    private val googleMapPackage = "com.google.android.apps.maps"
    private val DEFAULT_ZOOM_IN_LEVEL = 16.5f

    fun <T:View> bindView(@IdRes resId: Int): Lazy<T> = lazy { findViewById<T>(resId) }

    private val button : FloatingActionButton by bindView(R.id.location)
    private val name: TextView by bindView(R.id.name)
    private val address: TextView by bindView(R.id.address)
    private val mrt: TextView by bindView(R.id.mrt)
    private val site: TextView by bindView(R.id.site)
    private val socket: TextView by bindView(R.id.socket)
    private val limitTime: TextView by bindView(R.id.time_limit)
    private val standing: TextView by bindView(R.id.standing)
    private val ratingWifi: TextView by bindView(R.id.wifi)
    private val ratingSeat: TextView by bindView(R.id.seat)
    private val ratingTasty: TextView by bindView(R.id.tasty)
    private val ratingCheap: TextView by bindView(R.id.cheap)
    private val ratingQuiet: TextView by bindView(R.id.quiet)
    private val ratingMusic: TextView by bindView(R.id.music)
    private val openingTime: TextView by bindView(R.id.opening_time)

    private var coffeeShop:CoffeeShop? = null

    lateinit var map: GoogleMap
    lateinit var mapFragment: MapFragment

    private var mHelper: SwipeBackActivityHelper? = null

    companion object {
        val KEY_ID = "id"
        fun go(context: Context, id:String) {
            val intent = Intent(context, ShopDetailActivity::class.java)
            intent.putExtra(KEY_ID, id)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_detail)
        initSwipeBackActivityHelper()

        val coffeeShopsViewModel = ViewModelProviders.of(this).get(CoffeeShopsViewModel::class.java)
        initViewDataByIntent(coffeeShopsViewModel)

        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initSwipeBackActivityHelper() {
        mHelper = SwipeBackActivityHelper(this)
        mHelper!!.onActivityCreate()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHelper!!.onPostCreate()
    }

    override fun <T : View?> findViewById(id: Int): T {
        val v = super.findViewById<View>(id) as T
        return if (v == null && mHelper != null) mHelper!!.findViewById(id) as T else v
    }

    override fun getSwipeBackLayout(): SwipeBackLayout {
        return mHelper!!.swipeBackLayout
    }

    override fun setSwipeBackEnable(enable: Boolean) {
        swipeBackLayout.setEnableGesture(enable)
    }

    override fun scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this)
        swipeBackLayout.scrollToFinishActivity()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        initMapUiSetting()
        initShopMarker()
    }

    private fun initMapUiSetting() {
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
    }

    private fun initShopMarker() {
        coffeeShop?.let {
            val latLng: LatLng = LatLng(
                it.latitude?.toDouble() ?: 0.0,
                it.longitude?.toDouble() ?: 0.0
            )
            map.addMarker(MarkerOptions().position(latLng).title(it.name))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_IN_LEVEL))
        }
    }

    private fun initViewDataByIntent(coffeeShopsViewModel: CoffeeShopsViewModel) {
        intent.getStringExtra(KEY_ID)?.let {
            coffeeShopsViewModel.current.get(it)?.let { coffeeShop: CoffeeShop ->
                this@ShopDetailActivity.coffeeShop = coffeeShop
                name.text = coffeeShop.name
                address.text = coffeeShop.address
                button.setOnClickListener(buttonClickListener(coffeeShop))
                initSocket(coffeeShop.socket)
                initLimitTime(coffeeShop.limited_time)
                initStanding(coffeeShop.standing_desk)
                initMRT(coffeeShop.mrt)
                initSite(coffeeShop.url)
                initRating(coffeeShop)
                initOpeningTime(coffeeShop.open_time)
            } ?: run {
                showErrorToastAndFinish()
            }
        } ?: run {
            showErrorToastAndFinish()
        }
    }

    private fun buttonClickListener(coffeeShop: CoffeeShop): View.OnClickListener {
        return View.OnClickListener {
            val s = "geo:%s,%s?q=%s".format(coffeeShop.latitude.toString(), coffeeShop.longitude.toString(), coffeeShop.name)
            val gmmIntentUri = Uri.parse(s)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(googleMapPackage)
            startActivity(mapIntent)
        }
    }

    private fun initSocket(data: String?) {
        if (valid(data) && validState(data!!)) {
            socket.text = when(data) {
                "yes" -> getString(R.string.socket_yes)
                "no" -> getString(R.string.socket_no)
                "maybe" -> getString(R.string.socket_maybe)
                else -> getString(R.string.no_data)
            }
        } else {
            socket.visibility = View.GONE
        }
    }

    private fun initLimitTime(data: String?) {
        if (valid(data) && validState(data!!)) {
            limitTime.text = when(data) {
                "yes" -> getString(R.string.limit_time_yes)
                "no" -> getString(R.string.limit_time_no)
                "maybe" -> getString(R.string.limit_time_maybe)
                else -> getString(R.string.no_data)
            }
        } else {
            limitTime.visibility = View.GONE
        }
    }

    private fun initStanding(data: String?) {
        if (valid(data) && data.equals("yes")) {
            standing.text = getString(R.string.standing_yes)
        } else {
            standing.visibility = View.GONE
        }
    }

    private fun validState(data: String): Boolean {
        return data == "yes" || data == "no" || data == "maybe"
    }

    private fun initMRT(data: String?) {
        if (valid(data)) {
            mrt.text = if (data!!.length > 8) getString(R.string.info_arrival, data) else getString(R.string.info_mrt, data)
        } else {
            mrt.visibility = View.GONE
        }
    }

    private fun initSite(site: String?) {
        if (valid(site)) {
            var afterDecode = URLDecoder.decode(site, "UTF-8")
            this.site.text = afterDecode
            this.site.paint.flags = Paint.UNDERLINE_TEXT_FLAG
            this.site.setOnClickListener {
                if (!afterDecode.startsWith("http")) {
                    afterDecode = "https://$afterDecode"
                }
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(afterDecode)))
            }
        } else {
            this.site.visibility = View.GONE
        }
    }

    private fun initRating(coffeeShop: CoffeeShop) {
        ratingWifi.text = getString(R.string.rating_wifi, coffeeShop.wifi)
        ratingSeat.text = getString(R.string.rating_seat, coffeeShop.seat)
        ratingTasty.text = getString(R.string.rating_tasty, coffeeShop.tasty)
        ratingCheap.text = getString(R.string.rating_cheap, coffeeShop.cheap)
        ratingQuiet.text = getString(R.string.rating_quiet, coffeeShop.quiet)
        ratingMusic.text = getString(R.string.rating_music, coffeeShop.music)
    }

    private fun initOpeningTime(openTime: String?) {
        openingTime.text = if (valid(openTime)) openTime else getString(R.string.no_data)
    }

    private fun valid(input: String?): Boolean = input != null && input.isNotEmpty()

    private fun showErrorToastAndFinish() {
        Toast.makeText(this, R.string.shop_detail_invalid_id_msg, Toast.LENGTH_LONG).show()
        finish()
    }
}