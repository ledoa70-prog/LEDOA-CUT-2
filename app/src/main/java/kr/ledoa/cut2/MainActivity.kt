package kr.ledoa.cut2
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kr.ledoa.cut2.databinding.ActivityMainBinding
data class Clip(val id:Long,val uri:Uri,var startMs:Long=0L,var endMs:Long=Long.MAX_VALUE)
class MainActivity:AppCompatActivity(){
 private lateinit var b:ActivityMainBinding; private lateinit var player:ExoPlayer
 private val clips=mutableListOf<Clip>(); private var selected=-1
 private val picker=registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->
  uris.forEach{u->try{contentResolver.takePersistableUriPermission(u,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};clips+=Clip(System.nanoTime(),u)}
  rebuild(); if(selected<0&&clips.isNotEmpty())select(0)
 }
 override fun onCreate(s:Bundle?){super.onCreate(s);b=ActivityMainBinding.inflate(layoutInflater);setContentView(b.root)
  player=ExoPlayer.Builder(this).build();b.playerView.player=player
  player.addListener(object:Player.Listener{override fun onIsPlayingChanged(x:Boolean){b.playPause.text=if(x)"일시정지" else "재생"}})
  b.addMedia.setOnClickListener{picker.launch(arrayOf("video/*"))}
  b.playPause.setOnClickListener{if(player.isPlaying)player.pause()else player.play()}
  b.delete.setOnClickListener{if(selected in clips.indices){clips.removeAt(selected);selected=-1;player.stop();player.clearMediaItems();rebuild();if(clips.isNotEmpty())select(0)else b.emptyText.visibility=View.VISIBLE}}
  b.split.setOnClickListener{split()}
 }
 private fun select(i:Int){if(i !in clips.indices)return;selected=i;val c=clips[i];player.setMediaItem(MediaItem.fromUri(c.uri));player.prepare();player.seekTo(c.startMs);b.emptyText.visibility=View.GONE;rebuild()}
 private fun split(){if(selected !in clips.indices)return;val c=clips[selected];val p=player.currentPosition;if(p<=c.startMs+100)return;val e=c.endMs;c.endMs=p;clips.add(selected+1,Clip(System.nanoTime(),c.uri,p,e));rebuild()}
 private fun rebuild(){b.timeline.removeAllViews();clips.forEachIndexed{i,_->val v=TextView(this);v.text="  클립 "+(i+1)+"  ";v.textSize=16f;v.setTextColor(if(i==selected)0xFFFFD18A.toInt()else 0xFFFFFFFF.toInt());v.setPadding(18,12,18,12);v.setOnClickListener{select(i)};b.timeline.addView(v)}}
 override fun onStop(){super.onStop();player.pause()};override fun onDestroy(){player.release();super.onDestroy()}
}